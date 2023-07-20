package com.loafer.core.service.impl;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.loafer.core.common.ErrorCode;
import com.loafer.core.common.exception.BusinessException;
import com.loafer.core.config.OssProperties;
import com.loafer.core.dto.QrConfigDto;
import com.loafer.core.service.IOssService;
import com.loafer.core.service.IQrCodeService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@Service
public class QrCodeServiceImpl implements IQrCodeService {

    @Resource(name = "ossProperties")
    private OssProperties ossProperties;

    @Resource(name = "ossServiceImpl")
    private IOssService iOssService;

    @Resource(name = "modelMapper")
    private ModelMapper modelMapper;

    private final String logObjectName = "loafer-core/log/log.png";

    @Override
    public byte[] generatePng(QrConfigDto dto) throws BusinessException {
        InputStream logInputStream = null;
        try {
            logInputStream = iOssService.getObjectContent(ossProperties.getBucketName(), logObjectName);
            BufferedImage logImage = ImageIO.read(logInputStream);
            QrConfig config = new QrConfig();
            modelMapper.map(dto, config);
            config.setImg(logImage);
            return QrCodeUtil.generatePng(dto.getContent(), config);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "二维码生成失败！");
        } finally {
            if (logInputStream != null) {
                try {
                    logInputStream.close();
                } catch (IOException e) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "OSS INPUT_STREAM CLOSE FAIL！");
                }
            }
        }
    }
}
