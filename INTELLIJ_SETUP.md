# IntelliJ IDEA Kurulum Talimatları

## 1. Java SDK'yı 17'ye Ayarlama

1. **File** → **Project Structure** (⌘; veya `Cmd + ;`)
2. Sol tarafta **Project** sekmesine tıklayın
3. **SDK** dropdown'ından **17** seçin (yoksa **Add SDK** → **Download JDK** → **Version 17**)
4. **Language level** → **17 - Sealed types, always-strict floating-point semantics** seçin
5. **Apply** → **OK**

## 2. Annotation Processing'i Etkinleştirme

1. **File** → **Settings** (⌘, veya `Cmd + ,`)
2. Sol menüden: **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
3. ✅ **Enable annotation processing** kutusunu işaretleyin
4. **Apply** → **OK**

## 3. Maven Tool Window'dan Projeyi Yenileme

### Maven Tool Window'u Açma:
- Sağ tarafta **Maven** sekmesine tıklayın
- Yoksa: **View** → **Tool Windows** → **Maven**
- Kısayol: `⌘2` tuşuna basın, "Maven" yazın ve Enter

### Projeyi Yenileme:
- Maven tool window'da proje adının yanındaki **🔄 (Reload All Maven Projects)** butonuna tıklayın
- VEYA proje adına **sağ tıklayın** → **Reload Project**

## 4. Projeyi Yeniden Derleme

### Yöntem 1: Menüden
- **Build** → **Rebuild Project**
- Kısayol: `⌘⇧F9` (Mac) veya `Ctrl+Shift+F9` (Windows/Linux)

### Yöntem 2: Maven Tool Window'dan
- Maven tool window'da **Lifecycle** → **clean** çift tıklayın
- Sonra **Lifecycle** → **compile** çift tıklayın

## 5. Cache'i Temizleme (Gerekirse)

Eğer hala hatalar görüyorsanız:
1. **File** → **Invalidate Caches / Restart**
2. **Invalidate and Restart** butonuna tıklayın
3. IntelliJ yeniden başlayacak

## Kontrol Listesi

- [ ] Java SDK 17 ayarlandı
- [ ] Annotation Processing etkinleştirildi
- [ ] Maven projesi yenilendi
- [ ] Proje rebuild edildi
- [ ] Hatalar kayboldu

## Terminal'den Çalıştırma (Alternatif)

Eğer IntelliJ'de hala sorun varsa, terminal'den çalıştırabilirsiniz:

```bash
./run.sh
```

Bu script Java 17 kullanarak projeyi çalıştırır.








