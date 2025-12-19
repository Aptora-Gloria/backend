# Build ve IDE Sorun Giderme Rehberi

Bu doküman, projede Lombok ve diğer annotation'ların okunamamasından kaynaklanan build hatalarını çözmek için hazırlanmıştır.

## Hızlı Çözüm Kontrol Listesi

1. ✅ IDE'de Lombok plugin yüklü mü?
2. ✅ Annotation Processing aktif mi?
3. ✅ Maven projesi doğru import edildi mi?
4. ✅ IDE cache temizlendi mi?
5. ✅ Maven clean install çalıştırıldı mı?

---

## 1. IntelliJ IDEA için Çözümler

### Adım 1: Lombok Plugin Kurulumu

1. **File** → **Settings** (Windows/Linux) veya **IntelliJ IDEA** → **Preferences** (macOS)
2. **Plugins** sekmesine git
3. Arama kutusuna "Lombok" yaz
4. **Lombok** plugin'ini bul ve **Install** butonuna tıkla
5. IDE'yi **Restart** et

### Adım 2: Annotation Processing Aktifleştirme

1. **File** → **Settings** → **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
2. **Enable annotation processing** checkbox'ını işaretle
3. **Apply** ve **OK** butonlarına tıkla

### Adım 3: Maven Projesini Yeniden İçe Aktarma

1. **File** → **Invalidate Caches / Restart...**
2. **Invalidate and Restart** butonuna tıkla
3. IDE yeniden başladıktan sonra:
   - **File** → **Reload Gradle/Maven Project** (sağ üstte Maven ikonuna tıkla)
   - Veya terminal'de: `mvn clean install`

### Adım 4: Project Structure Kontrolü

1. **File** → **Project Structure** (Ctrl+Alt+Shift+S / Cmd+;)
2. **Project** sekmesinde:
   - **Project SDK**: Java 17 olmalı
   - **Project language level**: 17 olmalı
3. **Modules** sekmesinde:
   - Tüm modüllerin **Language level**'ı 17 olmalı

---

## 2. Eclipse için Çözümler

### Adım 1: Lombok Kurulumu

1. Lombok jar dosyasını indir: https://projectlombok.org/download
2. Terminal'de şu komutu çalıştır:
   ```bash
   java -jar lombok.jar
   ```
3. Açılan pencerede Eclipse kurulum dizinini seç
4. **Install / Update** butonuna tıkla
5. Eclipse'i yeniden başlat

### Adım 2: Annotation Processing Aktifleştirme

1. **Project** → **Properties**
2. **Java Compiler** → **Annotation Processing**
3. **Enable project specific settings** işaretle
4. **Enable annotation processing** işaretle
5. **Apply and Close**

### Adım 3: Projeyi Temizleme

1. **Project** → **Clean...**
2. Tüm projeleri seç
3. **Clean** butonuna tıkla

---

## 3. VS Code için Çözümler

### Adım 1: Java Extension Pack Kurulumu

1. VS Code'da **Extensions** (Ctrl+Shift+X / Cmd+Shift+X)
2. "Java Extension Pack" ara ve yükle
3. "Lombok Annotations Support for VS Code" ara ve yükle

### Adım 2: Settings.json Güncelleme

1. **File** → **Preferences** → **Settings**
2. Arama kutusuna "java.jdt.ls.lombokSupport" yaz
3. **Java: Lombok Support** seçeneğini **Enable** yap

Veya `settings.json` dosyasına ekle:
```json
{
  "java.jdt.ls.lombokSupport.enabled": true
}
```

### Adım 3: Java Language Server'ı Yeniden Başlatma

1. **Command Palette** (Ctrl+Shift+P / Cmd+Shift+P)
2. "Java: Clean Java Language Server Workspace" yaz ve çalıştır
3. VS Code'u yeniden başlat

---

## 4. Maven Komut Satırı Çözümleri

### Temiz Build

```bash
# Proje dizinine git
cd /path/to/backend

# Temizle ve derle
mvn clean install

# Eğer hala sorun varsa
mvn clean compile
```

### Annotation Processing Kontrolü

`pom.xml` dosyasında Lombok dependency'sinin doğru olduğundan emin ol:

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

### Maven Compiler Plugin Kontrolü

Eğer `pom.xml`'de özel compiler ayarları varsa, annotation processing'in aktif olduğundan emin ol:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.30</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

---

## 5. Yaygın Hata Mesajları ve Çözümleri

### Hata: "Cannot find symbol: method getX()" veya "Cannot find symbol: method setX()"

**Neden:** Lombok annotation'ları işlenmemiş

**Çözüm:**
1. IDE'de annotation processing'i aktifleştir (Yukarıdaki adımlar)
2. `mvn clean install` çalıştır
3. IDE cache'ini temizle

### Hata: "package lombok does not exist"

**Neden:** Lombok dependency'si eksik veya yanlış scope

**Çözüm:**
1. `pom.xml`'de Lombok dependency'sini kontrol et
2. `mvn dependency:resolve` çalıştır
3. IDE'de Maven projesini reload et

### Hata: "@RequiredArgsConstructor cannot be resolved"

**Neden:** Lombok import'u eksik veya annotation processing çalışmıyor

**Çözüm:**
1. Import statement'ı kontrol et: `import lombok.RequiredArgsConstructor;`
2. Annotation processing'i aktifleştir
3. IDE'yi yeniden başlat

### Hata: "The method builder() is undefined"

**Neden:** `@Builder` annotation'ı işlenmemiş

**Çözüm:**
1. Entity class'ında `@Builder` annotation'ının olduğundan emin ol
2. Annotation processing'i aktifleştir
3. `mvn clean compile` çalıştır

---

## 6. Proje Özel Kontroller

### Java Versiyonu

Proje Java 17 kullanıyor. IDE'nizde Java 17 SDK'nın yüklü olduğundan emin olun:

```bash
# Terminal'de kontrol et
java -version
# Çıktı: openjdk version "17.x.x" olmalı
```

### Maven Versiyonu

```bash
# Terminal'de kontrol et
mvn -version
# Maven 3.6+ olmalı
```

### Proje Yapısı

Proje yapısının doğru olduğundan emin ol:
```
backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── optimetric/
│   │   └── resources/
│   └── test/
```

---

## 7. Son Çare Çözümler

### Tüm Cache'leri Temizleme

**IntelliJ IDEA:**
```bash
# Terminal'de proje dizininde
rm -rf .idea/
rm -rf target/
mvn clean install
```

**Eclipse:**
```bash
rm -rf .metadata/
rm -rf .classpath
rm -rf .project
rm -rf .settings/
mvn clean install
```

**VS Code:**
```bash
rm -rf .vscode/
rm -rf target/
mvn clean install
```

### Maven Local Repository Temizleme (Son Çare)

```bash
# Dikkat: Bu tüm Maven dependency'lerini siler
rm -rf ~/.m2/repository/org/projectlombok/
mvn clean install
```

---

## 8. Yardım ve Destek

Eğer yukarıdaki çözümler işe yaramazsa:

1. **Hata mesajını tam olarak kopyala** (stack trace dahil)
2. **IDE versiyonunu belirt** (IntelliJ 2023.3, Eclipse 2023-09, vs.)
3. **Java versiyonunu belirt** (`java -version` çıktısı)
4. **Maven versiyonunu belirt** (`mvn -version` çıktısı)
5. **İşletim sistemini belirt** (Windows, macOS, Linux)

---

## 9. Önleyici Adımlar

### Yeni Projeyi İçe Aktarırken

1. IDE'yi açmadan önce terminal'de `mvn clean install` çalıştır
2. IDE'de projeyi aç
3. Maven projesini reload et
4. Annotation processing'i aktifleştir
5. IDE'yi restart et

### Takım Çalışması İçin

1. `.gitignore` dosyasında IDE-specific dosyalar ignore edilmeli:
   ```
   .idea/
   .vscode/
   .metadata/
   *.iml
   ```
2. `pom.xml` dosyası güncel tutulmalı
3. Lombok versiyonu takım içinde aynı olmalı

---

## 10. Hızlı Test

Aşağıdaki komutları çalıştırarak projenin doğru yapılandırıldığını test edebilirsiniz:

```bash
# Proje dizininde
mvn clean compile
# Başarılı olmalı, hata olmamalı

mvn clean test
# Testler çalışmalı

mvn clean package
# JAR dosyası oluşturulmalı
```

Eğer bu komutlar başarılı olursa, sorun IDE konfigürasyonundadır. Yukarıdaki IDE-specific çözümlere bakın.


