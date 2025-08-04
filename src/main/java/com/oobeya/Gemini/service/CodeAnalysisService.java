package com.oobeya.Gemini.service;
import com.oobeya.github.dto.response.ContentDto;
import java.util.List;

/**
 * GitHub repo kodlarını analiz etmek ve yapay zeka önerileri almak için servis arayüzü.
 */
public interface CodeAnalysisService {

    String analyzeFileAndGetRecommendations(String authHeader, String owner, String repoName, String filePath);

    String getCodeRecommendations(List<ContentDto> codeContents);
}