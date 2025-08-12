package com.oobeya.github.entity;

import com.oobeya.auth.entity.OwnerType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "github_repositories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"htmlUrl"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String name;

    @Column(length = 500)
    private String fullName;


    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 1000, unique = true)
    private String htmlUrl;

    private boolean fork;

    @Column(name = "login",nullable = false)
    private String login;

}

