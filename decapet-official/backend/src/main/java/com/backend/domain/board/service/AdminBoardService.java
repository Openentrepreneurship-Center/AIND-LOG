package com.backend.domain.board.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.global.common.PageResponse;
import com.backend.global.service.S3Service;

import com.backend.domain.board.dto.mapper.PostResponseMapper;
import com.backend.domain.board.dto.response.PostResponse;
import com.backend.domain.board.entity.Post;
import com.backend.domain.board.entity.PostType;
import com.backend.domain.board.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBoardService {

    private final PostRepository postRepository;
    private final PostResponseMapper postResponseMapper;
    private final S3Service s3Service;

    public PageResponse<PostResponse> getAllPosts(Pageable pageable) {
        return PageResponse.from(
            postRepository.findAll(pageable)
                .map(postResponseMapper::toResponse)
        );
    }

    public PageResponse<PostResponse> getPostsByType(PostType type, Pageable pageable) {
        return PageResponse.from(
            postRepository.findByType(type, pageable)
                .map(postResponseMapper::toResponse)
        );
    }

    public PageResponse<PostResponse> searchPosts(String keyword, PostType type, Pageable pageable) {
        if (type != null) {
            return PageResponse.from(
                postRepository.findByTypeAndTitleContainingIgnoreCaseOrTypeAndContentContainingIgnoreCase(
                    type, keyword, type, keyword, pageable)
                    .map(postResponseMapper::toResponse)
            );
        }
        return PageResponse.from(
            postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                keyword, keyword, pageable)
                .map(postResponseMapper::toResponse)
        );
    }

    public PostResponse getPost(String postId) {
        Post post = postRepository.findByIdOrThrow(postId);
        return postResponseMapper.toResponse(post);
    }

    @Transactional
    public void deletePost(String postId) {
        Post post = postRepository.findByIdOrThrow(postId);
        List<String> attachmentUrls = post.getAttachmentUrls();

        post.delete();

        // S3 삭제는 best-effort (실패해도 게시글은 이미 삭제됨)
        if (attachmentUrls != null && !attachmentUrls.isEmpty()) {
            s3Service.deleteFiles(attachmentUrls);
        }
    }
}
