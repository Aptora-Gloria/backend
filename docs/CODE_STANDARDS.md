# Kod ve dokümantasyon standartları

## 1. Yorum satırı standardı

- Yalnızca **tek satır yorum** kullanılır.
- Format: `// <yorum>`
- Maksimum uzunluk: **72 karakter** (boşluk dahil).
- Dil: **Türkçe**.
- Emoji, teşekkür veya gereksiz ifade kullanılmaz.

Örnek:

```java
// Kullanıcı giriş isteğini hazırla
```

## 2. Sınıf düzeni ve okunabilirlik

Tüm sınıflarda aşağıdaki sıra uygulanır:

1. Sabitler / static alanlar
2. Örnek alanları (fields)
3. Kurucular (constructors)
4. Public metotlar
5. Private yardımcı metotlar

Format:

- Metotlar arasında **tek boş satır**.
- Alan grupları arasında **tek boş satır**.
- Art arda **en fazla bir** boş satır (çift boş satır yok).
- 40–60 satırı geçen metotlar mantıksal parçalara bölünür.
- Her sınıf **tek sorumluluk** prensibine uygun olur.

## 3. Paket yapısı (modül bazlı)

Her modül aşağıdaki paketleri içerir:

- **controller** (veya api): HTTP endpoint’leri
- **application**: use case, port (in/out), DTO
- **domain**: model, domain service, value object
- **infrastructure**: persistence, http adapter, config, mapper
- **config**: modüle özel konfigürasyon
- **exception**: modüle özel hata sınıfları (veya shared)
- **mapper**: API / persistence mapping

Kurallar:

- **util** yalnızca modüle özel yardımcılar için kullanılır.
- Tekrarlayan mantık **shared** modüle taşınır.
- Gereksiz paketler kaldırılır.

## 4. Modül dokümantasyonu

Her modül için `docs/<modül>/docs/` altında:

- **README.md** – modül amacı
- **API.md** – endpoint / contract
- **DB.md** – tablolar ve kısıtlar
- **FLOW.md** – servis akışı

Dosya isimleri büyük harfle (README.md, API.md, DB.md, FLOW.md).
Modül dokümantasyonunda ownership belirtilir (örn. Auth docs owner: Mehmet).

## 5. Kod formatlama

Proje Spotless ile uyumlu olacak şekilde formatlanır. Build’de `mvn spotless:apply` çalıştırılabilir (Spotless eklendiğinde).

---

Bu standartlar iş mantığını değiştirmez; yalnızca yapı, okunabilirlik ve dokümantasyonu hedefler.
