# User Metrikleri - Repository Kapsamı Analizi

## 🔍 Mevcut Durum

**Şu anki implementasyon:**
```java
List<RepositoryEntity> repositories = repositoryRepository.findByUserOrderByPushedAtDesc(user);
```

Bu sorgu, **kullanıcının sistemde kayıtlı olan TÜM repo'ları** getiriyor.

### RepositoryEntity İlişkisi:
- `user_id` → Hangi kullanıcının repo listesinde görünüyor
- `owner_login` → Repo'nun gerçek sahibi (GitHub'da)
- `owner_type` → USER veya ORGANIZATION
- `is_private` → Private veya Public
- `is_fork` → Fork mu, original mu

---

## 🤔 Sorun ve Seçenekler

User metrikleri hesaplanırken hangi repo'lar dahil edilmeli?

### Senaryo Örnekleri:

**Kullanıcı: `john`**

1. **john/repo1** - Kendi repo'su (owner_login = "john")
2. **company/repo2** - Organization repo'su (owner_login = "company", owner_type = ORGANIZATION)
3. **otheruser/repo3** - Başkasının repo'su (owner_login = "otheruser")
4. **john/forked-repo** - Fork edilmiş repo (is_fork = true)
5. **john/private-repo** - Private repo (is_private = true)

**Soru:** User metrikleri hesaplanırken hangileri dahil edilmeli?

---

## 📋 Seçenekler

### Seçenek 1: TÜM Repo'lar (Mevcut Durum) ✅

**Kapsam:**
- Kullanıcının sistemde kayıtlı olan TÜM repo'lar
- Kendi repo'ları + Organization repo'ları + Fork'lar + Private/Public hepsi

**Avantajlar:**
- ✅ En kapsamlı metrikler
- ✅ Kullanıcının tüm aktivitesini gösterir
- ✅ Basit implementasyon

**Dezavantajlar:**
- ❌ Fork'lar ve organization repo'ları kullanıcının kendi aktivitesini yansıtmayabilir
- ❌ Metrikler "gürültülü" olabilir

**Kullanım Senaryosu:**
- Kullanıcının genel GitHub aktivitesini görmek istiyorsa

---

### Seçenek 2: Sadece Kendi Repo'ları (owner_login == user.login)

**Kapsam:**
- Sadece kullanıcının owner olduğu repo'lar
- `owner_login == user.login` ve `owner_type == USER`

**Avantajlar:**
- ✅ Kullanıcının gerçek sahipliğindeki repo'lar
- ✅ Daha temiz ve anlamlı metrikler
- ✅ Kişisel performans metrikleri

**Dezavantajlar:**
- ❌ Organization'daki katkılar görünmez
- ❌ Fork'lar dahil değil (eğer fork'ları da istiyorsak)

**Kullanım Senaryosu:**
- Kullanıcının kendi projelerindeki performansını görmek istiyorsa

---

### Seçenek 3: Kendi Repo'ları + Organization Repo'ları

**Kapsam:**
- `owner_login == user.login` (kendi repo'ları)
- VEYA `owner_type == ORGANIZATION` (organization repo'ları)

**Avantajlar:**
- ✅ Hem kişisel hem de iş aktivitesi
- ✅ Daha dengeli metrikler

**Dezavantajlar:**
- ❌ Organization'daki tüm repo'lar dahil (sadece katkıda bulunduğu değil)

**Kullanım Senaryosu:**
- Hem kişisel hem de iş aktivitesini görmek istiyorsa

---

### Seçenek 4: Sadece Original Repo'lar (Fork Değil)

**Kapsam:**
- `is_fork == false` olan tüm repo'lar

**Avantajlar:**
- ✅ Sadece original projeler
- ✅ Fork'ların metrikleri karışmaz

**Dezavantajlar:**
- ❌ Fork'lar tamamen hariç (bazı fork'lar aktif olabilir)

**Kullanım Senaryosu:**
- Sadece original projelerdeki aktiviteyi görmek istiyorsa

---

### Seçenek 5: Sadece Public Repo'lar

**Kapsam:**
- `is_private == false` olan tüm repo'lar

**Avantajlar:**
- ✅ Sadece açık kaynak aktivitesi
- ✅ Toplulukla paylaşılan projeler

**Dezavantajlar:**
- ❌ Private projelerdeki aktivite görünmez

**Kullanım Senaryosu:**
- Sadece public aktiviteyi görmek istiyorsa

---

### Seçenek 6: Kombinasyon (Önerilen) ⭐

**Kapsam:**
- `owner_login == user.login` (kendi repo'ları)
- VEYA `owner_type == ORGANIZATION` (organization repo'ları)
- VE `is_fork == false` (fork'lar hariç, opsiyonel)

**Avantajlar:**
- ✅ En mantıklı ve dengeli seçenek
- ✅ Kullanıcının gerçek katkılarını gösterir
- ✅ Fork'ları dahil edip etmeme opsiyonel

**Dezavantajlar:**
- ❌ Biraz daha karmaşık filtreleme

**Kullanım Senaryosu:**
- Genel kullanım için en uygun seçenek

---

## 🎯 Öneri

### Senaryo 1: Genel Kullanım (Önerilen)
```java
// Kendi repo'ları + Organization repo'ları (fork hariç)
repositories = repositoryRepository.findByUserOrderByPushedAtDesc(user)
    .stream()
    .filter(repo -> 
        (repo.getOwnerLogin().equals(user.getLogin()) && repo.getOwnerType() == OwnerType.USER)
        || repo.getOwnerType() == OwnerType.ORGANIZATION
    )
    .filter(repo -> !Boolean.TRUE.equals(repo.getIsFork())) // Fork'ları hariç tut
    .toList();
```

### Senaryo 2: Sadece Kendi Repo'ları
```java
// Sadece kullanıcının owner olduğu repo'lar
repositories = repositoryRepository.findByUserOrderByPushedAtDesc(user)
    .stream()
    .filter(repo -> 
        repo.getOwnerLogin().equals(user.getLogin()) 
        && repo.getOwnerType() == OwnerType.USER
    )
    .toList();
```

### Senaryo 3: Tüm Repo'lar (Mevcut)
```java
// Hiçbir filtreleme yok, tüm repo'lar
repositories = repositoryRepository.findByUserOrderByPushedAtDesc(user);
```

---

## 🔧 Implementasyon Önerisi

### Seçenek A: Parametreli Filtreleme
```java
public UserMetricsDto getUserMetrics(String login, RepoFilter filter) {
    // filter: ALL, OWNED, ORGANIZATION, NO_FORKS, PUBLIC, etc.
}
```

### Seçenek B: Varsayılan Filtreleme (Önerilen)
```java
// Varsayılan: Kendi repo'ları + Organization (fork hariç)
// İleride parametre eklenebilir
```

### Seçenek C: Ayrı Endpoint'ler
```java
POST /api/metrics/user/all              // Tüm repo'lar
POST /api/metrics/user/owned            // Sadece kendi repo'ları
POST /api/metrics/user/organization     // Sadece organization
```

---

## 📊 Karşılaştırma Tablosu

| Seçenek | Kendi Repo | Org Repo | Fork | Private | Karmaşıklık |
|---------|-----------|----------|------|---------|-------------|
| **1. Tüm Repo'lar** | ✅ | ✅ | ✅ | ✅ | Düşük |
| **2. Sadece Kendi** | ✅ | ❌ | ✅ | ✅ | Düşük |
| **3. Kendi + Org** | ✅ | ✅ | ✅ | ✅ | Orta |
| **4. Original (No Fork)** | ✅ | ✅ | ❌ | ✅ | Orta |
| **5. Public Only** | ✅ | ✅ | ✅ | ❌ | Orta |
| **6. Kombinasyon** | ✅ | ✅ | ❌ | ✅ | Yüksek |

---

## 💡 Sonuç ve Öneri

**Önerilen Yaklaşım: Seçenek 6 (Kombinasyon)**

1. **Varsayılan:** Kendi repo'ları + Organization repo'ları (fork hariç)
2. **İleride:** Request parametresi ile filtreleme seçenekleri eklenebilir
3. **RepositoryRepository'ye yeni metod eklenebilir:**
   ```java
   List<RepositoryEntity> findByUserAndFilters(User user, boolean includeForks, boolean includePrivate);
   ```

Bu şekilde hem varsayılan davranış mantıklı olur, hem de ileride esneklik sağlanır.

