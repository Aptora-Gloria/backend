# Lombok Build Hataları - Hızlı Çözüm

Eğer projede Lombok annotation'ları (örn: `@RequiredArgsConstructor`, `@Builder`, `@Getter`, `@Setter`) okunamıyorsa, aşağıdaki adımları takip edin.

## IntelliJ IDEA (En Yaygın)

1. **Lombok Plugin Yükle:**
   - `File` → `Settings` → `Plugins` → "Lombok" ara → `Install` → IDE'yi restart et

2. **Annotation Processing Aktifleştir:**
   - `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
   - ✅ `Enable annotation processing` işaretle → `Apply` → `OK`

3. **Cache Temizle:**
   - `File` → `Invalidate Caches / Restart...` → `Invalidate and Restart`

4. **Maven Reload:**
   - Sağ üstteki Maven ikonuna tıkla veya `File` → `Reload Gradle/Maven Project`

## Eclipse

1. **Lombok Kur:**
   - https://projectlombok.org/download adresinden `lombok.jar` indir
   - Terminal'de: `java -jar lombok.jar`
   - Eclipse kurulum dizinini seç → `Install / Update` → Eclipse'i restart et

2. **Annotation Processing:**
   - `Project` → `Properties` → `Java Compiler` → `Annotation Processing`
   - ✅ `Enable project specific settings` ve ✅ `Enable annotation processing` işaretle

3. **Projeyi Temizle:**
   - `Project` → `Clean...` → Tüm projeleri seç → `Clean`

## VS Code

1. **Extensions Yükle:**
   - "Java Extension Pack" ve "Lombok Annotations Support for VS Code" yükle

2. **Settings:**
   - `File` → `Preferences` → `Settings` → "java.jdt.ls.lombokSupport" ara
   - ✅ `Java: Lombok Support` aktif et

3. **Language Server Temizle:**
   - `Ctrl+Shift+P` → "Java: Clean Java Language Server Workspace" → VS Code'u restart et

## Maven Komut Satırı

```bash
# Proje dizininde
mvn clean install
```

Eğer hala hata alıyorsanız:
```bash
mvn clean compile
```

## Hızlı Test

Terminal'de şu komutu çalıştırın:
```bash
mvn clean compile
```

✅ Başarılı olursa → Sorun IDE konfigürasyonunda (yukarıdaki adımları uygulayın)  
❌ Hata verirse → Detaylı çözüm için `TROUBLESHOOTING.md` dosyasına bakın

## Yaygın Hatalar

| Hata | Çözüm |
|------|-------|
| `Cannot find symbol: method getX()` | Annotation processing aktifleştir |
| `package lombok does not exist` | Maven projesini reload et |
| `@RequiredArgsConstructor cannot be resolved` | Lombok plugin yükle + IDE restart |

---

**Detaylı çözümler için:** `TROUBLESHOOTING.md` dosyasına bakın.


