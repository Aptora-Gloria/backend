# IntelliJ Hatalarını Düzeltme - Detaylı Adımlar

Eğer hala hatalar görüyorsanız, aşağıdaki adımları **SIRASIYLA** uygulayın:

## 🔴 ÖNEMLİ: Önce Lombok Plugin'ini Kontrol Edin

1. **File** → **Settings** (⌘,)
2. **Plugins** → **Installed** sekmesine gidin
3. **"Lombok"** arayın
4. Eğer yüklü değilse: **Marketplace** sekmesine gidin → **"Lombok"** arayın → **Install**
5. **Apply** → **OK**
6. IntelliJ'yi **yeniden başlatın** (Restart IDE)

## 1. Maven Import Ayarlarını Kontrol Edin

1. **File** → **Settings** (⌘,)
2. **Build, Execution, Deployment** → **Build Tools** → **Maven** → **Importing**
3. ✅ **Use Maven wrapper** işaretli olmalı (eğer mvnw dosyası varsa)
4. ✅ **Automatically download**: **Sources** ve **Documentation** işaretli olmalı
5. **Apply** → **OK**

## 2. Maven Projesini Tamamen Yeniden İçe Aktarın

1. Maven tool window'u açın (sağ tarafta **Maven** sekmesi)
2. Proje adına **sağ tıklayın**
3. **Unimport Maven Project** seçin
4. Tekrar **sağ tıklayın** → **Reload Project**
5. VEYA: **File** → **Invalidate Caches / Restart** → **Invalidate and Restart**

## 3. Annotation Processing Ayarlarını Kontrol Edin

1. **File** → **Settings** (⌘,)
2. **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
3. ✅ **Enable annotation processing** işaretli olmalı
4. **Store generated files relative to**: **Module content root** seçili olmalı
5. **Generated sources directory**: `target/generated-sources/annotations` olmalı
6. **Apply** → **OK**

## 4. Proje Yapısını Kontrol Edin

1. **File** → **Project Structure** (⌘;)
2. **Project** sekmesi:
   - **SDK**: **17** seçili olmalı
   - **Language level**: **17** seçili olmalı
3. **Modules** sekmesi:
   - **backend** modülü seçili olmalı
   - **Language level**: **17** olmalı
4. **Apply** → **OK**

## 5. Projeyi Tamamen Temizleyin ve Yeniden Derleyin

### Terminal'den:
```bash
cd /Users/oguzhanderya/Desktop/aptoria
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
mvn clean
mvn compile
```

### IntelliJ'den:
1. **Build** → **Clean Project**
2. **Build** → **Rebuild Project** (⌘⇧F9)

## 6. IntelliJ Cache'ini Temizleyin

1. **File** → **Invalidate Caches / Restart**
2. **Invalidate and Restart** butonuna tıklayın
3. IntelliJ yeniden başlayacak

## 7. Lombok Plugin Ayarlarını Kontrol Edin

1. **File** → **Settings** (⌘,)
2. **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
3. **Annotation Processors** bölümünde:
   - ✅ **Enable annotation processing**
   - **Store generated files relative to**: **Module content root**
   - **Generated sources directory**: `target/generated-sources/annotations`

## 8. Maven Tool Window'dan Lifecycle'ı Çalıştırın

1. Maven tool window'u açın
2. **Lifecycle** klasörünü açın
3. **clean**'e **çift tıklayın** (temizler)
4. **compile**'a **çift tıklayın** (derler)
5. **package**'a **çift tıklayın** (paketler)

## Kontrol Listesi

- [ ] Lombok plugin yüklü ve etkin
- [ ] IntelliJ yeniden başlatıldı
- [ ] Java SDK 17 ayarlandı
- [ ] Annotation Processing etkinleştirildi
- [ ] Maven projesi yeniden import edildi
- [ ] Proje clean ve rebuild edildi
- [ ] Cache temizlendi
- [ ] Maven lifecycle (clean, compile) çalıştırıldı

## Hala Çalışmıyorsa

Eğer yukarıdaki adımların hepsini uyguladıysanız ve hala hatalar varsa:

1. IntelliJ'yi tamamen kapatın
2. Terminal'den projeyi derleyin:
   ```bash
   ./run.sh
   ```
3. Eğer terminal'den derleme başarılıysa, sorun IntelliJ yapılandırmasındadır
4. IntelliJ'yi açın ve **File** → **Open** → Proje klasörünü **yeniden açın**








