# Language Unique Constraint Test Rehberi

Bu rehber, language unique constraint görevinin test edilmesi için adım adım talimatlar içerir.

## Ön Hazırlık

### 1. PostgreSQL Bağlantısı
DB bilgileri (`application.yml`'den):
- **Host:** localhost:5432
- **Database:** optimetric
- **User:** optimetric_user
- **Password:** optimetric_pass

### 2. PostgreSQL'e Bağlanma Yöntemleri

#### Yöntem 1: psql (Terminal)
```bash
psql -h localhost -p 5432 -U optimetric_user -d optimetric
# Şifre sorulduğunda: optimetric_pass
```

#### Yöntem 2: Docker Container (Eğer docker-compose kullanıyorsan)
```bash
docker exec -it optimetric-postgres psql -U optimetric_user -d optimetric
```

#### Yöntem 3: GUI Tool (DBeaver, pgAdmin, DataGrip, vs.)
- Host: localhost
- Port: 5432
- Database: optimetric
- User: optimetric_user
- Password: optimetric_pass

---

## Test Adımları

### Adım 1: Duplicate Kontrolü (Migration Öncesi)

**Amaç:** Mevcut duplicate'leri görmek

**SQL Sorgusu:**
```sql
-- scripts/check_duplicate_languages.sql dosyasındaki sorgu
SELECT repository_id, language_name, COUNT(*) AS cnt
FROM github_repository_languages
GROUP BY repository_id, language_name
HAVING COUNT(*) > 1
ORDER BY cnt DESC;
```

**Nasıl Çalıştırılır:**

**Terminal (psql):**
```bash
# 1. psql'e bağlan
psql -h localhost -p 5432 -U optimetric_user -d optimetric

# 2. SQL dosyasını çalıştır
\i scripts/check_duplicate_languages.sql

# VEYA direkt sorguyu yapıştır:
SELECT repository_id, language_name, COUNT(*) AS cnt
FROM github_repository_languages
GROUP BY repository_id, language_name
HAVING COUNT(*) > 1
ORDER BY cnt DESC;
```

**GUI Tool:**
1. SQL Editor'ü aç
2. `scripts/check_duplicate_languages.sql` dosyasını aç veya içeriğini kopyala
3. Execute/Run butonuna bas

**Beklenen Sonuç:**
- Eğer duplicate varsa: Her duplicate için satır gösterir (örn: repository_id=1, language_name='Java', cnt=5)
- Eğer duplicate yoksa: Boş sonuç döner (0 rows)

---

### Adım 2: Migration'ları Çalıştır

**Amaç:** Duplicate'leri temizlemek ve unique constraint eklemek

**Not:** Spring Boot uygulaması başlatıldığında Flyway otomatik olarak migration'ları çalıştırır.

**Manuel Çalıştırma (Opsiyonel):**

**Terminal (psql):**
```bash
# V002 migration'ı çalıştır
psql -h localhost -p 5432 -U optimetric_user -d optimetric -f src/main/resources/db/migration/V002__deduplicate_github_repository_languages.sql

# V003 migration'ı çalıştır
psql -h localhost -p 5432 -U optimetric_user -d optimetric -f src/main/resources/db/migration/V003__add_unique_constraint_repository_language.sql
```

**GUI Tool:**
1. Her migration dosyasını aç
2. İçeriğini SQL Editor'e yapıştır
3. Execute/Run

**Beklenen Sonuç:**
- V002: "DELETE X" mesajı (kaç satır silindi)
- V003: "ALTER TABLE" başarılı

---

### Adım 3: Duplicate Kontrolü (Migration Sonrası)

**Amaç:** Duplicate'lerin temizlendiğini doğrulamak

**Aynı sorguyu tekrar çalıştır:**
```sql
SELECT repository_id, language_name, COUNT(*) AS cnt
FROM github_repository_languages
GROUP BY repository_id, language_name
HAVING COUNT(*) > 1
ORDER BY cnt DESC;
```

**Beklenen Sonuç:**
- **0 rows** dönmeli (duplicate yok)

---

### Adım 4: API Testi (Idempotency Test)

**Amaç:** Aynı repo için `/api/github/languages/init` endpoint'ini 3 kez çağırarak duplicate oluşmadığını doğrulamak

#### Yöntem 1: Postman

1. **Postman'i aç**
2. **Yeni Request oluştur:**
   - Method: `POST`
   - URL: `http://localhost:8080/api/github/languages/init`
   - Headers:
     - `Content-Type: application/json`
     - `Authorization: Bearer <JWT_TOKEN>` (JWT token'ını al)
   - Body (raw JSON):
     ```json
     {
       "repoFullName": "facebook/react"
     }
     ```

3. **3 kez Send butonuna bas** (aynı request'i 3 kez gönder)

4. **Her seferinde 200 OK dönmeli**

#### Yöntem 2: cURL (Terminal)

```bash
# JWT token'ını al (önce login olman gerekiyor)
# Token'ı TOKEN değişkenine ata:
TOKEN="your_jwt_token_here"

# 1. İlk çağrı
curl -X POST http://localhost:8080/api/github/languages/init \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"repoFullName": "facebook/react"}'

# 2. İkinci çağrı (aynı repo)
curl -X POST http://localhost:8080/api/github/languages/init \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"repoFullName": "facebook/react"}'

# 3. Üçüncü çağrı (aynı repo)
curl -X POST http://localhost:8080/api/github/languages/init \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"repoFullName": "facebook/react"}'
```

**Beklenen Sonuç:**
- Her çağrı 200 OK dönmeli
- Response'da language listesi dönmeli

---

### Adım 5: DB Kontrolü (Her Language İçin Tek Satır)

**Amaç:** API testinden sonra DB'de her language için tek satır olduğunu doğrulamak

**1. Test ettiğin repo'nun ID'sini bul:**
```sql
SELECT id, full_name 
FROM repositories 
WHERE full_name = 'facebook/react';
-- Örnek: id = 1
```

**2. O repo için language'leri kontrol et:**
```sql
-- <repo_id> yerine yukarıdaki sorgudan bulduğun ID'yi yaz
SELECT repository_id, language_name, COUNT(*) AS cnt, MAX(bytes) AS max_bytes
FROM github_repository_languages
WHERE repository_id = <repo_id>  -- Örnek: WHERE repository_id = 1
GROUP BY repository_id, language_name
ORDER BY language_name;
```

**Beklenen Sonuç:**
- Her `language_name` için **cnt = 1** olmalı
- `max_bytes` son çağrıdaki bytes değerini yansıtmalı

**3. Tüm duplicate'leri kontrol et (genel):**
```sql
SELECT repository_id, language_name, COUNT(*) AS cnt
FROM github_repository_languages
GROUP BY repository_id, language_name
HAVING COUNT(*) > 1;
```

**Beklenen Sonuç:**
- **0 rows** (hiç duplicate olmamalı)

---

### Adım 6: Unique Constraint Testi

**Amaç:** Unique constraint'in çalıştığını doğrulamak

**Manuel Insert Denemesi:**
```sql
-- Önce mevcut bir kaydı bul
SELECT id, repository_id, language_name 
FROM github_repository_languages 
LIMIT 1;
-- Örnek: repository_id = 1, language_name = 'Java'

-- Aynı repository_id ve language_name ile yeni kayıt eklemeyi dene
INSERT INTO github_repository_languages (repository_id, language_name, bytes, percentage)
VALUES (1, 'Java', 1000, 10.5);
```

**Beklenen Sonuç:**
- **HATA** almalısın:
  ```
  ERROR: duplicate key value violates unique constraint "uk_repo_language"
  DETAIL: Key (repository_id, language_name)=(1, Java) already exists.
  ```

Bu hata, unique constraint'in çalıştığını gösterir! ✅

---

## Özet Kontrol Listesi

- [ ] Adım 1: Migration öncesi duplicate kontrolü yapıldı
- [ ] Adım 2: Migration'lar çalıştırıldı (V002, V003)
- [ ] Adım 3: Migration sonrası duplicate kontrolü yapıldı (0 duplicate olmalı)
- [ ] Adım 4: API testi yapıldı (3 kez çağrıldı, hepsi başarılı)
- [ ] Adım 5: DB kontrolü yapıldı (her language için cnt=1)
- [ ] Adım 6: Unique constraint testi yapıldı (hata alındı ✅)

---

## Sorun Giderme

### "psql: command not found" hatası
```bash
# macOS
brew install postgresql

# Linux
sudo apt-get install postgresql-client
```

### "Connection refused" hatası
- Docker container'ın çalıştığından emin ol:
  ```bash
  docker ps | grep postgres
  ```
- Eğer çalışmıyorsa:
  ```bash
  docker-compose up -d
  ```

### JWT Token nasıl alınır?
1. Frontend'den login ol
2. Browser DevTools > Application > Local Storage > JWT token'ı kopyala
3. Veya login endpoint'ini çağır ve response'dan token'ı al

---

## İpuçları

1. **SQL sorgularını kaydet:** Her test için kullandığın sorguları bir dosyaya kaydet, tekrar kullanmak için
2. **Test repo seç:** Küçük bir repo seç (örn: kendi test repo'n), büyük repo'lar uzun sürebilir
3. **Logları izle:** Spring Boot console loglarını izle, hata varsa görebilirsin
4. **Backup al:** Önemli veriler varsa, migration öncesi DB backup'ı al
