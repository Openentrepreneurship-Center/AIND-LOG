package com.backend.domain.board.dto.internal;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.board.entity.PostType;


public record CreatePostInfo(
    String userId,
    PostType type,
    String title,
    String content,
    List<MultipartFile> files
) {
}
