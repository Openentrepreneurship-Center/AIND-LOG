package com.backend.domain.board.entity;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import com.backend.domain.user.entity.User;
import com.backend.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_posts_user_id", columnList = "user_id")
})
@SQLRestriction("deleted_at IS NULL")
public class Post extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> attachmentUrls;

    @Builder
    public Post(User user, PostType type, String title, String content, List<String> attachmentUrls) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.attachmentUrls = attachmentUrls;
    }

    public void anonymize() {
        this.title = "삭제된 게시글";
        this.content = null;
        this.attachmentUrls = null;
    }
}
