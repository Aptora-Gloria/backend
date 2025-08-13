package com.oobeya.github.entity;

import com.oobeya.github.util.BranchUid;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "github_branches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255 ,unique = true)
    private String uid;  //  UID (repo_html_url + branch_name)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repo_html_url", referencedColumnName = "htmlUrl", nullable = false)
    private RepositoryEntity repository;

    @PrePersist
    @PreUpdate
    private void generateUid() {
        this.uid = BranchUid.of(repository.getHtmlUrl(), this.name);
    }
}
