// src/main/java/com/oobeya/Gemini/service/impl/CodeAnalysisServiceImpl.java
package com.oobeya.Gemini.service.impl;
import com.oobeya.Gemini.constant.LanguageExtension;
import com.oobeya.github.dto.ContentDto;
import com.oobeya.github.exception.GitHubApiException;
import com.oobeya.Gemini.service.CodeAnalysisService;
import com.oobeya.github.service.ContentServiceI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.oobeya.github.util.TokenUtil;

import static com.oobeya.Gemini.constant.GeminiConstans.ERROR_EMPTY_CODE_LIST;


@Service
@Slf4j // Corrected annotation
@RequiredArgsConstructor
public class CodeAnalysisServiceImpl implements CodeAnalysisService {

    private final WebClient geminiWebClient;

    private final ContentServiceI  contentServiceI;
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    private static final String GEMINI_API_URL_PATH = "/v1beta/models/gemini-2.0-flash:generateContent";
    private static final int MAX_LLM_INPUT_CHARS = 500000000; // Yaklaşık 25K karakter
    @Override
    public String analyzeFileAndGetRecommendations(
            String authHeader,
            String owner,
            String repoName,
            String filePath) {

        String jwtToken = TokenUtil.extractToken(authHeader);

        List<ContentDto> fileContent = contentServiceI.getContentFromDb(authHeader,repoName);

        return this.getCodeRecommendations(fileContent); // Analyze only the specific file content
    }

    @Override
    public String getCodeRecommendations(List<ContentDto> codeContents) {

        if (codeContents.isEmpty()) {return ERROR_EMPTY_CODE_LIST;}

        //todo: Effective log = bilgi seviyesine uygun log seviyesi (info, warn, error) kullanmak ve gerektiğinde context bilgisi eklemek
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Aşağıdaki GitHub deposu kodlarını analiz edin ve iki ana başlık altında önerilerde bulunun:\n\n");
        promptBuilder.append("1.  **Kod Kalitesi ve İyileştirmeler**: Kodun okunabilirliği, sürdürülebilirliği, performans, güvenlik açıkları ve genel tasarım prensipleri açısından değerlendirmeler yapın. Yeniden düzenleme (refactoring), daha iyi pratikler ve yaygın hatalardan kaçınma konularında spesifik önerilerde bulunun.\n\n");
        promptBuilder.append("2.  **Genel Mimari ve Tasarım Yaklaşımları**: Deponun genel mimarisine, kullanılan kütüphanelere/framework'lere ve tasarım desenlerine odaklanın. Ölçeklenebilirlik, esneklik ve modülerlik açısından potansiyel iyileştirmeler veya alternatif yaklaşımlar önerin.\n\n");
        promptBuilder.append("Her öneri başlığı altında madde işaretleri kullanarak kısa ve öz maddeler halinde bilgi verin. Gerekirse her madde için kısa bir açıklama veya örnek sunun. Türkçeye çevirilmiş, anlaşılır ve eyleme geçirilebilir öneriler sunmaya özen gösterin. Yalnızca önerileri ve açıklamaları markdown formatında döndürün, başka bir metin eklemeyin.\n");
        promptBuilder.append("--- Kod İçeriği Başlangıcı ---\n\n");

        long totalChars = 0;
        for (ContentDto file : codeContents) {
            String fileSection =  String.format("### Dosya: %s\n```%s\n```\n\n", file.name(), getLanguageExtension(file.path()));
            if (totalChars + fileSection.length() > MAX_LLM_INPUT_CHARS) {
                log.warn("LLM giriş karakter limitine ulaşıldı ({} karakter). Kalan dosyalar atlanıyor.", MAX_LLM_INPUT_CHARS);
                break;
            }
            promptBuilder.append(fileSection);
            totalChars += fileSection.length();
        }
        promptBuilder.append("--- Kod İçeriği Sonu ---\n");

        String prompt = promptBuilder.toString();
        log.info("LLM'e gönderilen prompt'un toplam karakter sayısı: {}", prompt.length());

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt))))
            );
            Map<String, Object> result = geminiWebClient
                    .post()
                    .uri(uriBuilder -> uriBuilder.path(GEMINI_API_URL_PATH).queryParam("key", geminiApiKey).build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class).flatMap(errorBody -> {
                                log.error("Gemini API'den öneri çekme hatası ({}): {}", response.statusCode(), errorBody);
                                return Mono.error(new GitHubApiException( errorBody));
                            })
                    )
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            // === AI'den gelen cevabı çözümleme ===
            if (result == null || !result.containsKey("candidates")) return error();

            Object candidatesObj = result.get("candidates");
            if (!(candidatesObj instanceof List<?> candidates) || candidates.isEmpty()) return error();

            Object firstCandidate = candidates.get(0);
            if (!(firstCandidate instanceof Map<?, ?> rawCandidate)) return error();

            Map<String, Object> firstCandidateMap = (Map<String, Object>) rawCandidate;
            Object contentObj = firstCandidateMap.get("content");
            if (!(contentObj instanceof Map<?, ?> rawContent)) return error();

            Map<String, Object> content = (Map<String, Object>) rawContent;
            Object partsObj = content.get("parts");
            if (!(partsObj instanceof List<?> parts) || parts.isEmpty()) return error();

            Object firstPart = parts.get(0);
            if (!(firstPart instanceof Map<?, ?> rawFirstPart)) return error();

            Map<String, Object> firstPartMap = (Map<String, Object>) rawFirstPart;
            Object textObj = firstPartMap.get("text");
            if (textObj instanceof String text) return text;

            return error();

        } catch (Exception e) {
            log.error("Yapay zekadan kod önerileri alınırken hata oluştu: {}", e.getMessage());
            throw new GitHubApiException( e.getMessage());
        }
    }

    private String error() {
        return "Yapay zekadan öneri alınamadı. Yanıt yapısı beklenenden farklı.";
    }
    private String getLanguageExtension(String filePath) {

        if (filePath == null || filePath.lastIndexOf('.') == -1) {
            return "";
        }
        String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase(); // Uzantıyı küçük harfe çevir
        Optional<LanguageExtension> langExt = LanguageExtension.fromExtension(extension);
        return langExt.map(LanguageExtension::getLanguageName).orElse("");
    }
}