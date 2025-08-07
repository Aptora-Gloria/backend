package com.oobeya.github.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "github_organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    private String description;

    @Column(name = "avatar_url")
    private String avatarUrl;

    //@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    //private List<RepositoryEntity> repositories; // ilişki için hazır ama şimdilik boş kalabilir
}
