package com.oobeya.Gemini.dto; // Bu satırı ekleyin

import lombok.Data;

@Data
public class FileAnalysisRequest {
    private String owner;
    private String repoName;
    private String filePath;
}