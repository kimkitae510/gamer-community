package com.gamercommunity.genre.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "genre")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Genre(String name) {
        validateName(name);
        this.name = name;
    }

    // 검증 로직
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("장르 이름은 필수입니다.");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("장르 이름은 50자를 초과할 수 없습니다.");
        }
    }

    // 장르 이름 수정
    public void updateName(String name) {
        validateName(name);
        this.name = name;
    }
}

