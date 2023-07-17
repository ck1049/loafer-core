package com.loafer.core.model;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author loafer
 * @since 2023-07-17
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("file_storage")
@ApiModel(value = "FileStorage对象", description = "")
public class FileStorage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("文件id")
    private String id;

    @ApiModelProperty("文件名")
    private String fileName;

    @ApiModelProperty("文件大小")
    private Long fileSize;

    @ApiModelProperty("文件类型")
    private String fileType;

    @ApiModelProperty("文件url")
    private String filePath;

    @ApiModelProperty("上传人id")
    private Long uploaderId;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    public FileStorage(String id, String fileName, Long fileSize, String fileType, String filePath, Long uploaderId) {
        this.id = id;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.filePath = filePath;
        this.uploaderId = uploaderId;
    }
}
