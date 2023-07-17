package com.loafer.core.service.impl;

import cn.hutool.core.date.DateUtil;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.PutObjectResult;
import com.loafer.core.common.ErrorCode;
import com.loafer.core.common.exception.BusinessException;
import com.loafer.core.config.OssProperties;
import com.loafer.core.enums.ContentTypeDict;
import com.loafer.core.mapper.FileStorageMapper;
import com.loafer.core.model.FileStorage;
import com.loafer.core.model.UserInfo;
import com.loafer.core.service.IFileManageService;
import com.loafer.core.service.IOssService;
import com.loafer.core.service.IUsersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
public class FileManageServiceImpl implements IFileManageService {

    @Resource(name = "ossProperties")
    private OssProperties ossProperties;

    @Resource(name = "ossServiceImpl")
    private IOssService iOssService;

    @Resource(name = "usersServiceImpl")
    private IUsersService iUsersService;

    @Resource
    private FileStorageMapper fileStorageMapper;

    @Value("${spring.application.name}")
    private String appName;

    @Override
    public List<String> upload(MultipartFile[] files) {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        String tokenId = ((ServletRequestAttributes) requestAttributes).getRequest().getHeader("tokenId");
        UserInfo userInfo = iUsersService.getLoginUser(tokenId);
        // ArrayList非线程安全，可能出现null值、索引越界、元素丢失等问题
        List<String> fileIds = Collections.synchronizedList(new ArrayList<>());
        if (ArrayUtils.isEmpty(files)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件为空！");
        }
        Arrays.asList(files).parallelStream()
                .filter(file -> file != null && !file.isEmpty())
                .forEach(file -> uploadAndSaveToDb(file, fileIds, userInfo));
        return fileIds;
    }

    private void uploadAndSaveToDb(MultipartFile file, List<String> fileIds, UserInfo userInfo) {

        String fileName = file.getOriginalFilename();
        String bucketName = ossProperties.getBucketName();
        try {
            String contentType = file.getContentType();
            ContentTypeDict contentTypeDict = ContentTypeDict.getByCentType(contentType);
            if (contentTypeDict == null && StringUtils.isNotBlank(contentType)) {
                log.info("暂不支持该类型({})文件上传！", contentType);
                return;
            }

            InputStream inputStream = file.getInputStream();
            String yyyyMMdd = DateUtil.format(new Date(), "yyyyMMdd");
            String timeStamp = DateUtil.format(new Date(), "yyyyMMddHHmmssSSS");

            // 文件后缀名
            String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
            // 存储到oss之后的文件名
            String ossFileName = "".equals(suffix) ? fileName + timeStamp : fileName.replace(suffix, timeStamp + suffix);
            // oss的ObjectName 即文件存储路径，不含bucketName
            String objectName = StringUtils.joinWith( "/", appName, StringUtils.isBlank(contentType) ? "others" : contentType.split("/")[0], yyyyMMdd, ossFileName);
            PutObjectResult putObjectResult = iOssService.putObject(bucketName, objectName, inputStream);
            // 设置文件公开读权限
            iOssService.setObjectAcl(bucketName, objectName, CannedAccessControlList.PublicRead);
            String fileId = UUID.randomUUID().toString();
            //String filePath = iOssService.generatorUrl(bucketName, objectName, DateUtil.offsetMinute(new Date(), 5)).getPath();
            String url = ossProperties.getEndpointInterNet().replace("://", "://" + bucketName + ".");
            url = StringUtils.join(Arrays.asList(url, objectName), "/");
            FileStorage entity = new FileStorage(fileId, fileName, file.getSize(), suffix, url, userInfo.getId());
            fileStorageMapper.insert(entity);
            fileIds.add(fileId);
        } catch (IOException e) {
            e.printStackTrace();
            log.info("====== 文件:${}$上传失败! =======", fileName);
        }
    }
}
