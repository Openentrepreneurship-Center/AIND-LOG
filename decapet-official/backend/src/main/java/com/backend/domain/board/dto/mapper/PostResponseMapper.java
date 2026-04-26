package com.backend.domain.board.dto.mapper;

import org.springframework.stereotype.Component;

import com.backend.domain.board.dto.response.PostResponse;
import com.backend.domain.board.entity.Post;
import com.backend.global.util.HtmlSanitizer;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostResponseMapper {

    private final HtmlSanitizer htmlSanitizer;

    public PostResponse toResponse(Post post) {
        var user = post.getUser();
        return new PostResponse(
                post.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getPhone() : null,
                post.getType(),
                htmlSanitizer.sanitize(post.getTitle()),
                htmlSanitizer.sanitize(post.getContent()),
                post.getAttachmentUrls(),
                post.getCreatedAt()
        );
    }
}
