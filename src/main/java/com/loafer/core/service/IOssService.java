package com.loafer.core.service;

import com.aliyun.oss.model.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Oss操作服务
 */
public interface IOssService {

    /**
     * 创建存储空间
     * @param bucketName
     * @return
     */
    Bucket createBucket(String bucketName);

    /**
     * 通过流式上传的方式将简单文件上传，最大不能超过5GB
     * @param bucketName
     * @param objectName
     * @param inputStream
     * @return
     */
    PutObjectResult putObject(String bucketName, String objectName, InputStream inputStream, Callback... callbacks);

    /**
     * 简单文件上传，最大不能超过5GB
     * @param bucketName
     * @param objectName
     * @param file
     * @return
     */
    PutObjectResult putObject(String bucketName, String objectName, File file, Callback... callbacks);

    /**
     * 表单上传是使用HTML表单形式上传文件（Object）到指定存储空间（Bucket）中，文件最大不能超过5 GB。
     * @param bucketName
     * @param objectName
     * @param localFilePath
     * @return
     */
    String postObject(String bucketName, String objectName, String localFilePath) throws Exception;

    /**
     * 追加上传文件，最大不能超过5GB
     * 第一次追加时appendObjectResult=new ，第二次开始每次追加appendObjectResult使用上次追加的结果
     * @param bucketName
     * @param objectName
     * @param inputStream
     * @param meta
     * @return
     */
    AppendObjectResult appendObjectRequest(String bucketName, String objectName, AppendObjectResult appendObjectResult, InputStream inputStream, ObjectMetadata meta, Callback... callbacks);

    /**
     * 断点续传上传，支持并发、断点续传、自定义分片大小。大文件上传推荐使用断点续传。最大不能超过48.8TB。
     * @param bucketName
     * @param objectName Object完整路径（例如exampledir/exampleobject.txt），Object完整路径中不能包含Bucket名称。
     * @param localFilePath
     * @param meta
     */
    UploadFileResult checkPointUploadFile(String bucketName, String objectName, String localFilePath, ObjectMetadata meta, Callback... callbacks) throws Throwable;

    /**
     * 分片上传，当文件较大时，可以使用分片上传，最大不能超过48.8TB。
     * OSS提供的分片上传（Multipart Upload）功能，将要上传的较大文件（Object）分成多个分片（Part）来分别上传，上传完成后再调用CompleteMultipartUpload接口将这些Part组合成一个Object来达到断点续传的效果。
     * @param bucketName
     * @param objectName
     * @param file
     * @param meta
     * @return
     */
    CompleteMultipartUploadResult completeMultipartUpload(String bucketName, String objectName, File file, ObjectMetadata meta) throws IOException;

    /**
     * 通过流式下载方式从OSS下载文件
     * @param bucketName
     * @param objectName
     * @return
     */
    InputStream getObjectContent(String bucketName, String objectName);

    /**
     * 将存储空间（Bucket）中的文件（Object）下载到本地文件。
     * @param bucketName
     * @param objectName
     * @param file
     * @return
     */
    ObjectMetadata getObject(String bucketName, String objectName, File file);

    /**
     * 如果仅需要文件中的部分数据，您可以使用范围下载，下载指定范围内的数据。
     * 取[rangeStart, rangeEnd]字节范围内的数据，包括rangeStart和rangeEnd，共rangeEnd-rangeStart+1个字节的数据。
     * rangeBehavior false，如果指定的范围无效（比如开始或结束位置的指定值为负数，或指定值大于文件大小），则下载整个文件。
     * rangeBehavior true，在请求中增加请求头x-oss-range-behavior:standard，则改变指定范围不在有效区间时OSS的下载行为。假设现有大小为1000 Bytes的Object：
     *                  若指定了Range: bytes=500~2000，此时范围末端取值不在有效区间，返回500~999字节范围内容，且HTTP Code为206。
     *                  若指定了Range: bytes=1000~2000，此时范围首端取值不在有效区间，返回HTTP Code为416，错误码为InvalidRange。
     * @param bucketName
     * @param objectName
     * @return
     */
    InputStream getObjectContent(String bucketName, String objectName, int rangeStart, int rangeEnd, Boolean rangeBehavior);

    /**
     * 断点续传下载
     * 当下载大文件时，如果网络不稳定或者程序异常退出，会导致下载失败，甚至重试多次仍无法完成下载。为此，OSS提供了断点续传下载功能。
     * 断点续传下载将需要下载的文件分成若干个分片分别下载，所有分片都下载完成后，将所有分片合并成完整的文件。
     * @param bucketName
     * @param objectName
     * @param filePath
     * @return
     */
    DownloadFileResult downloadFile(String bucketName, String objectName, String filePath) throws Throwable;

    /**
     * 限定条件下载，If-Modified-Since和If-Unmodified-Since可以同时存在。If-Match和If-None-Match也可以同时存在。
     * If-Modified-Since	如果指定的时间早于实际修改时间，则正常传输文件，否则返回错误（304 Not modified）。
     * If-Unmodified-Since	如果指定的时间等于或者晚于文件实际修改时间，则正常传输文件，否则返回错误（412 Precondition failed）。
     * If-Match	如果指定的ETag和OSS文件的ETag匹配，则正常传输文件，否则返回错误（412 Precondition failed）。
     * If-None-Match	如果指定的ETag和OSS文件的ETag不匹配，则正常传输文件，否则返回错误（304 Not modified）。
     * @param bucketName
     * @param objectName
     * @param file
     * @return
     */
    ObjectMetadata getObject(String bucketName, String objectName, File file, Date modifiedSince, Date unmodifiedSince, Boolean match);

    /**
     * 列举bucketName存储空间下的文件，默认列举100个文件
     * @param bucketName
     * @return
     */
    List<OSSObjectSummary> getObjectSummaries(String bucketName);

    /**
     * 于删除指定文件
     * @param bucketName
     * @param objectName
     */
    VoidResult deleteObject(String bucketName, String objectName);

    /**
     * 生成文件的URL
     * @param bucketName
     * @param objectName
     * @param expire
     * @return
     */
    URL generatorUrl(String bucketName, String objectName, Date expire);

    /**
     * 获取存储空间的读写权限
     * @param bucketName
     * @return
     */
    AccessControlList getBucketAcl(String bucketName);

    /**
     * 设置存储空间的读写权限
     * @param bucketName
     * @param acl
     */
    VoidResult setBucketAcl(String bucketName, CannedAccessControlList acl);

    /**
     * 获取文件对象的读写权限
     * @param bucketName
     * @param objectName
     * @return
     */
    ObjectAcl getObjectAcl(String bucketName, String objectName);

    /**
     * 设置文件对象的读写权限
     * @param bucketName
     * @param objectName
     * @param acl
     */
    VoidResult setObjectAcl(String bucketName, String objectName, CannedAccessControlList acl);

}
