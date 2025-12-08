#!/bin/bash

# Build ve IDE Sorun Giderme Script'i
# BUILD_TROUBLESHOOTING.md dokümanına göre hazırlanmıştır

echo "🔧 Build ve IDE Sorun Giderme Başlatılıyor..."
echo ""

# 1. Maven Clean Install
echo "📦 1. Maven projesini temizleyip yeniden derleniyor..."
./mvnw clean install -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Maven build başarısız oldu!"
    exit 1
fi
echo "✅ Maven build başarılı"
echo ""

# 2. IDE Cache Temizleme (IntelliJ için)
echo "🧹 2. IDE cache dosyaları temizleniyor..."
if [ -d ".idea" ]; then
    # Cache dosyalarını temizle ama yapılandırma dosyalarını koru
    find .idea -name "*.xml" -path "*cache*" -delete 2>/dev/null
    find .idea -name "workspace.xml" -delete 2>/dev/null
    find .idea -name "tasks.xml" -delete 2>/dev/null
    echo "✅ IDE cache temizlendi"
else
    echo "ℹ️  .idea klasörü bulunamadı (normal, ilk açılış olabilir)"
fi
echo ""

# 3. Target klasörünü temizle (tekrar)
echo "🗑️  3. Target klasörü temizleniyor..."
rm -rf target/generated-sources/annotations/*
echo "✅ Target annotations klasörü temizlendi"
echo ""

# 4. Maven tekrar compile (annotation processing için)
echo "🔨 4. Annotation processing ile yeniden derleniyor..."
./mvnw clean compile
if [ $? -ne 0 ]; then
    echo "❌ Compile başarısız oldu!"
    exit 1
fi
echo "✅ Annotation processing ile compile başarılı"
echo ""

echo "✨ İşlem tamamlandı!"
echo ""
echo "📋 Sonraki Adımlar:"
echo "1. IntelliJ IDEA'yı tamamen kapatın"
echo "2. IntelliJ IDEA'yı yeniden açın"
echo "3. File → Invalidate Caches / Restart → Invalidate and Restart"
echo "4. File → Reload Gradle/Maven Project (sağ üstte Maven ikonuna tıklayın)"
echo ""
echo "Veya terminal'den:"
echo "  code .  # VS Code için"
echo ""




