package com.backend.domain.board.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.board.dto.internal.CreatePostInfo;
import com.backend.domain.board.dto.request.CreatePostRequest;
import com.backend.domain.board.entity.Post;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final UserRepository userRepository;

    public CreatePostInfo toCreatePostInfo(String userId, CreatePostRequest request, List<MultipartFile> files) {
        return new CreatePostInfo(
                userId,
                request.type(),
                request.title(),
                request.content(),
                files
        );
    }

    public Post toEntity(CreatePostInfo info, List<String> attachmentUrls) {
        User user = userRepository.getReferenceById(info.userId());

        return Post.builder()
                .user(user)
                .type(info.type())
                .title(info.title())
                .content(info.content())
                .attachmentUrls(attachmentUrls)
                .build();
    }
}
