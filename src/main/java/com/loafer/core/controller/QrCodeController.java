package com.loafer.core.controller;

import com.loafer.core.dto.QrConfigDto;
import com.loafer.core.service.IQrCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@Api(tags = "二维码")
@RestController
@RequestMapping("qrCode")
public class QrCodeController {

    @Resource(name = "qrCodeServiceImpl")
    private IQrCodeService service;


    @ApiOperation(value = "二维码生成")
    @GetMapping("generate")
    public ResponseEntity<byte[]> generate(@Valid QrConfigDto dto, HttpServletResponse response) throws IOException {
        byte[] bytes = service.generatePng(dto);
        response.getOutputStream().write(bytes);;
        /*BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        ImageIO.write(image, "png", new File("C:\\Users\\18238\\Desktop\\1.png"));*/
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

}
