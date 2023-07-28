package com.loafer.core.service;

import com.loafer.core.dto.SimpleMailMessageDto;

/**
 * 电子邮件
 */
public interface IEmailService {

    /**
     * 发送邮件
     * @param dto
     */
    void sendEmail(SimpleMailMessageDto dto);
}
