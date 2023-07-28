package com.loafer.core.controller;

import com.loafer.core.dto.SimpleMailMessageDto;
import com.loafer.core.service.IEmailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "邮件")
@RestController
@RequestMapping("email")
public class EmailController {

    @Resource(name = "emailServiceImpl")
    private IEmailService service;

    @ApiOperation("发送简单邮件")
    @PostMapping("sendMessage")
    public ResponseEntity<Boolean> sendMessage(@Valid @RequestBody SimpleMailMessageDto dto) {
        service.sendEmail(dto);
        return ResponseEntity.ok(true);
    }
}
