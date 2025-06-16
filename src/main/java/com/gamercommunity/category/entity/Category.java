package com.gamercommunity.category.entity;

import com.gamercommunity.genre.entity.Genre;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "board_category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean writable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Category> children = new ArrayList<>();


    private Set<Genre> genres = new HashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;


    @Builder
    public Category(String name, boolean writable, Category parent, Set<Genre> genres) {
        this.name = name;
        this.writable = writable;
        this.genres = genres != null ? genres : new HashSet<>();
        this.parent = parent;
    }

}
