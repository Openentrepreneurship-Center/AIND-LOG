package com.backend.domain.board.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.board.dto.response.PostResponse;
import com.backend.domain.board.entity.PostType;
import com.backend.domain.board.service.AdminBoardService;
import com.backend.global.common.PageResponse;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/posts")
@RequiredArgsConstructor
public class AdminBoardController implements AdminBoardApi {

    private final AdminBoardService adminBoardService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getAllPosts(
            @RequestParam(required = false) PostType type,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        PageResponse<PostResponse> response;
        if (keyword != null && !keyword.isBlank()) {
            response = adminBoardService.searchPosts(keyword, type, pageable);
        } else if (type != null) {
            response = adminBoardService.getPostsByType(type, pageable);
        } else {
            response = adminBoardService.getAllPosts(pageable);
        }
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.POST_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<SuccessResponse> getPost(@PathVariable String postId) {
        PostResponse response = adminBoardService.getPost(postId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.POST_GET_SUCCESS, response));
    }

    @Override
    @DeleteMapping("/{postId}")
    public ResponseEntity<SuccessResponse> deletePost(@PathVariable String postId) {
        adminBoardService.deletePost(postId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_DELETE_SUCCESS));
    }
}
