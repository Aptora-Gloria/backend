package com.oobeya.Gemini.Util;

// LanguageExtension enum'unu doğru paketten import ettiğinizden emin olun.
import com.oobeya.Gemini.constant.LanguageExtension;

import java.util.Optional;

public class GeminiUtil {

    /**
     * Verilen dosya yolundan dil uzantısını çıkarır ve ilgili programlama dilinin adını döndürür.
     * Örneğin: "myFile.java" -> "java", "script.js" -> "javascript", "document.txt" -> "text".
     * Desteklenmeyen veya uzantısı olmayan dosyalar için boş bir string döndürür.
     *
     * @param filePath Analiz edilecek dosya yolu.
     * @return Dosya uzantısına karşılık gelen dil adı veya boş string.
     */
    public static String getLanguageExtension(String filePath) {
        // Dosya yolu null veya uzantısı yoksa boş string döndür.
        if (filePath == null || filePath.lastIndexOf('.') == -1) {
            return "";
        }

        // Dosya uzantısını al ve küçük harfe çevir.
        String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();

        // LanguageExtension enum'ını kullanarak dil adını bul.
        Optional<LanguageExtension> langExt = LanguageExtension.fromExtension(extension);

        // Eğer bir eşleşme bulunursa dil adını döndür, aksi takdirde boş string döndür.
        return langExt.map(LanguageExtension::getLanguageName).orElse("");
    }
}