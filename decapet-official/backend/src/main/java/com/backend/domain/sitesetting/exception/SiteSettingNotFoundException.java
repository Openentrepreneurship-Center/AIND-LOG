package com.backend.domain.sitesetting.exception;

import com.backend.global.error.ErrorCode;
import com.backend.global.error.exception.BusinessException;

public class SiteSettingNotFoundException extends BusinessException {

    public SiteSettingNotFoundException() {
        super(ErrorCode.SITE_SETTING_NOT_FOUND);
    }
}
