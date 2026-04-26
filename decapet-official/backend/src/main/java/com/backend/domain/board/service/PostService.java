package com.backend.domain.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.domain.board.dto.internal.CreatePostInfo;
import com.backend.domain.board.dto.mapper.PostMapper;
import com.backend.domain.board.dto.mapper.PostResponseMapper;
import com.backend.domain.board.dto.response.PostResponse;
import com.backend.domain.board.entity.Post;
import com.backend.domain.board.repository.PostRepository;
import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;
import com.backend.global.service.S3Service;
import com.backend.global.util.HtmlSanitizer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final String POST_ATTACHMENT_DIRECTORY = "posts/attachments";

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final PostResponseMapper postResponseMapper;
    private final S3Service s3Service;
    private final TransactionTemplate transactionTemplate;
    private final HtmlSanitizer htmlSanitizer;

    public PostResponse createPost(CreatePostInfo info) {
        if (info.files() != null && info.files().size() > 5) {
            throw new BusinessException(ErrorCode.ATTACHMENT_LIMIT_EXCEEDED);
        }

        CreatePostInfo sanitizedInfo = new CreatePostInfo(
                info.userId(),
                info.type(),
                htmlSanitizer.sanitize(info.title()),
                htmlSanitizer.sanitize(info.content()),
                info.files()
        );

        List<String> attachmentUrls = s3Service.uploadFiles(sanitizedInfo.files(), POST_ATTACHMENT_DIRECTORY);

        try {
            return transactionTemplate.execute(status -> {
                Post post = postMapper.toEntity(sanitizedInfo, attachmentUrls);
                Post savedPost = postRepository.save(post);
                return postResponseMapper.toResponse(savedPost);
            });
        } catch (Exception e) {
            s3Service.deleteFiles(attachmentUrls);
            throw e;
        }
    }
}
