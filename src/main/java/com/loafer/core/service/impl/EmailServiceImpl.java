package com.loafer.core.service.impl;

import com.loafer.core.dto.SimpleMailMessageDto;
import com.loafer.core.service.IEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
public class EmailServiceImpl implements IEmailService {

    @Resource
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String username;

    @Override
    public void sendEmail(SimpleMailMessageDto dto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(dto.getTo());
        message.setSubject(dto.getSubject());
        message.setText(dto.getText());
        javaMailSender.send(message);
    }
}
