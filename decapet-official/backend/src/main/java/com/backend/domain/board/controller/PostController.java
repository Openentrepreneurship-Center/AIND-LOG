package com.backend.domain.board.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.domain.board.dto.mapper.PostMapper;
import com.backend.domain.board.dto.request.CreatePostRequest;
import com.backend.domain.board.dto.response.PostResponse;
import com.backend.domain.board.service.PostService;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController implements PostApi {

    private final PostService postService;
    private final PostMapper postMapper;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SuccessResponse> createPost(
            @AuthenticationPrincipal String userId,
            @Valid @RequestPart("data") CreatePostRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        PostResponse response = postService.createPost(postMapper.toCreatePostInfo(userId, request, files));
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.POST_CREATE_SUCCESS, response));
    }
}
