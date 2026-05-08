package com.backend.domain.user.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.domain.user.dto.request.BulkUpdatePermissionsRequest;
import com.backend.domain.user.dto.request.UpdateBuyerGradeRequest;
import com.backend.domain.user.dto.request.UpdatePermissionsRequest;
import com.backend.domain.user.dto.response.AdminUserListResponse;
import com.backend.domain.user.service.AdminUserService;
import com.backend.global.common.PageResponse;
import com.backend.global.common.SuccessCode;
import com.backend.global.common.SuccessResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController implements AdminUserApi {

    private final AdminUserService adminUserService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse> getUsers(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<AdminUserListResponse> users = adminUserService.getUsers(searchText, startDate, endDate, pageable);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_LIST_SUCCESS, users));
    }

    @Override
    @PatchMapping("/{userId}/admin-info")
    public ResponseEntity<SuccessResponse> updateUserAdminInfo(
            @PathVariable String userId,
            @RequestBody @Valid UpdateBuyerGradeRequest request) {
        adminUserService.updateUserAdminInfo(userId, request.buyerGrade(), request.adminMemo(), request.adminMemo2());
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_ADMIN_INFO_UPDATE_SUCCESS));
    }

    @Override
    @PatchMapping("/{userId}/permissions")
    public ResponseEntity<SuccessResponse> updatePermissions(
            @PathVariable String userId,
            @RequestBody @Valid UpdatePermissionsRequest request) {
        adminUserService.updatePermissions(userId, request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_PERMISSIONS_UPDATE_SUCCESS));
    }

    @PatchMapping("/permissions/bulk")
    public ResponseEntity<SuccessResponse> bulkUpdatePermissions(
            @RequestBody @Valid BulkUpdatePermissionsRequest request) {
        adminUserService.bulkUpdatePermissions(request);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.USER_PERMISSIONS_BULK_UPDATE_SUCCESS));
    }

    @Override
    @DeleteMapping("/{userId}")
    public ResponseEntity<SuccessResponse> deleteUser(@PathVariable String userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok(SuccessResponse.of(SuccessCode.ADMIN_DELETE_SUCCESS));
    }

}
