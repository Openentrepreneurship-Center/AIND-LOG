package com.backend.global.error.exception;

import com.backend.global.error.ErrorCode;

public class FileUploadFailedException extends BusinessException {
    public FileUploadFailedException() {
        super(ErrorCode.FILE_UPLOAD_FAILED);
    }
}
