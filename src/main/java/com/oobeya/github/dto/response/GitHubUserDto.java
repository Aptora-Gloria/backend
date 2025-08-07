package com.oobeya.github.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitHubUserDto {
    private String name;
    private String email;
    private String login;

    @JsonProperty("avatar_url")  
    private String avatarUrl;

    @JsonProperty("type")
    private String type;
}
