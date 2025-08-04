package com.oobeya.Gemini.constant;

import java.util.Arrays;
import java.util.Optional;

public enum LanguageExtension {
    JAVA("java", "java"),
    JAVASCRIPT("js", "javascript"),
    TYPESCRIPT("ts", "typescript"),
    PYTHON("py", "python"),
    C("c", "c"),
    CPP("cpp", "cpp"),
    H_HEADER("h", "c"), // C/C++ header files
    CSHARP("cs", "csharp"),
    GO("go", "go"),
    RUBY("rb", "ruby"),
    PHP("php", "php"),
    HTML("html", "html"),
    CSS("css", "css"),
    JSON("json", "json"),
    XML("xml", "xml"),
    YAML("yml", "yaml"), // Handles .yml
    YML_ALT("yaml", "yaml"), // Handles .yaml
    SQL("sql", "sql"),
    MARKDOWN("md", "markdown"),
    KOTLIN("kt", "kotlin"),
    BASH("sh", "bash"),
    TEXT("txt", "text"),
    LOG("log", "text"),
    UNKNOWN("", ""); // Default for unsupported or unknown extensions

    private final String fileExtension;
    private final String languageName;

    LanguageExtension(String fileExtension, String languageName) {
        this.fileExtension = fileExtension;
        this.languageName = languageName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getLanguageName() {
        return languageName;
    }

    /**
     * Given a file extension string, returns the corresponding LanguageExtension enum.
     * @param ext The file extension (e.g., "java", "js", "yml")
     * @return An Optional containing the LanguageExtension enum if found, otherwise empty.
     */
    public static Optional<LanguageExtension> fromExtension(String ext) {
        if (ext == null || ext.isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(le -> le.fileExtension.equalsIgnoreCase(ext))
                .findFirst();
    }
}