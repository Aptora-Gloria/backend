# IDE Hatalarını Düzeltme - Hızlı Talimatlar

Maven build başarılı ama IDE'de hatalar görüyorsanız, aşağıdaki adımları **SIRASIYLA** uygulayın:

## 🚀 Hızlı Çözüm (2 Dakika)

### 1. Script'i Çalıştır
```bash
./fix-ide-errors.sh
```

### 2. IntelliJ IDEA'da Yapılacaklar

1. **File** → **Invalidate Caches / Restart...**
   - **Invalidate and Restart** butonuna tıklayın
   - IDE yeniden başlayacak (30 saniye)

2. IDE yeniden başladıktan sonra:
   - Sağ üstte **Maven** ikonuna tıklayın
   - VEYA: **File** → **Reload Gradle/Maven Project**

3. **Build** → **Rebuild Project** (⌘⇧F9 / Ctrl+Shift+F9)

## ✅ Kontrol

Script'i çalıştırdıktan ve IDE'yi yeniden başlattıktan sonra:

- Lombok hataları kaybolmalı
- Import hataları düzelmeli
- Kırmızı alt çizgiler kaybolmalı

## ❌ Hala Hatalar Varsa

Detaylı çözümler için `BUILD_TROUBLESHOOTING.md` dosyasına bakın.

---

**Not:** Maven build başarılıysa (`./mvnw clean compile`), sorun kesinlikle IDE konfigürasyonundadır. Yukarıdaki adımlar %99 çözüm sağlar.




