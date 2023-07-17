package com.loafer.core.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件管理
 */
public interface IFileManageService {

    /**
     * 文件上传通用接口
     * @param files
     * @return
     */
    List<String> upload(MultipartFile[] files);
}
