package com.loafer.core.service;

import com.loafer.core.dto.QrConfigDto;

/**
 * 二维码服务
 */
public interface IQrCodeService {

    /**
     * png格式图片字节数组
     * @param dto
     * @return
     */
    byte[] generatePng(QrConfigDto dto);
}
