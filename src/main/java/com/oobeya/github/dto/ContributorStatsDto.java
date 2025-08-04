package com.oobeya.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContributorStatsDto {

    private Author author;
    private int total;
    private List<Week> weeks;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Author {

        @JsonProperty("login")
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Week {

        @JsonProperty("w")
        private long week;

        @JsonProperty("a")
        private int additions;

        @JsonProperty("d")
        private int deletions;

        @JsonProperty("c")
        private int commits;
    }
}
