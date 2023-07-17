package com.loafer.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    private String endpointInterNet;
    private String endpoint;
    private String bucketName;
    private String accessKeyId;
    private String secretAccessKey;
}
