package com.loafer.core.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.event.ProgressListener;
import com.aliyun.oss.internal.OSSUtils;
import com.aliyun.oss.model.*;
import com.loafer.core.config.OssProperties;
import com.loafer.core.listener.GetObjectProgressListener;
import com.loafer.core.service.IOssService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

@Slf4j
@Service
public class OssServiceImpl implements IOssService {

    @Resource(name = "putObjectProgressListener")
    private ProgressListener putObjectProgressListener;
    
    @Resource(name = "getObjectProgressListener")
    private GetObjectProgressListener getObjectProgressListener;

    @Resource(name = "ossProperties")
    private OssProperties ossProperties;

    @Resource(name = "ossClient")
    private OSS ossClient;

    @Override
    public Bucket createBucket(String bucketName) {
        return ossClient.createBucket(bucketName);
    }

    @Override
    public PutObjectResult putObject(String bucketName, String objectName, InputStream inputStream, Callback... callbacks) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream)
                .withProgressListener(putObjectProgressListener);
        if (ArrayUtils.isNotEmpty(callbacks)) {
            putObjectRequest.setCallback(callbacks[0]);
        }
        return ossClient.putObject(putObjectRequest);
    }

    @Override
    public PutObjectResult putObject(String bucketName, String objectName, File file, Callback... callbacks) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file).withProgressListener(putObjectProgressListener);
        if (ArrayUtils.isNotEmpty(callbacks)) {
            putObjectRequest.setCallback(callbacks[0]);
        }
        return ossClient.putObject(putObjectRequest);
    }

    @Override
    public String postObject(String bucketName, String objectName, String localFilePath) throws Exception {
        // 在URL中添加Bucket名称，添加后URL格式为http://yourBucketName.oss-cn-hangzhou.aliyuncs.com。
        String urlStr = ossProperties.getEndpoint()
                .replace("http://", "http://" + bucketName+ ".")
                .replace("https://", "https://" + bucketName+ ".");
        // 设置表单Map。
        Map<String, String> formFields = new LinkedHashMap<String, String>();
        // 设置文件名称。
        formFields.put("key", objectName);
        // 设置Content-Disposition。
        formFields.put("Content-Disposition", "attachment;filename=" + localFilePath);
        // 设置回调参数。
        // Callback callback = new Callback();
        // 设置回调服务器地址，例如http://oss-demo.oss-cn-hangzhou.aliyuncs.com:23450或http://127.0.0.1:9090。
        // callback.setCallbackUrl(callbackServerUrl);
        // 设置回调请求消息头中Host的值，如oss-cn-hangzhou.aliyuncs.com。
        // callback.setCallbackHost(callbackServerHost);
        // 设置发起回调时请求Body的值。
        // callback.setCallbackBody("{\\\"mimeType\\\":${mimeType},\\\"size\\\":${size}}");
        // 设置发起回调请求的Content-Type。
        // callback.setCalbackBodyType(Callback.CalbackBodyType.JSON);
        // 设置发起回调请求的自定义参数，由Key和Value组成，Key必须以x:开始，且必须小写。
        // callback.addCallbackVar("x:var1", "value1");
        // callback.addCallbackVar("x:var2", "value2");
        // 在表单Map中设置回调参数。
        // setCallBack(formFields, callback);
        // 设置OSSAccessKeyId。
        formFields.put("OSSAccessKeyId", ossProperties.getAccessKeyId());
        String policy = "{\"expiration\": \"2120-01-01T12:00:00.000Z\",\"conditions\": [[\"content-length-range\", 0, 104857600]]}";
        String encodePolicy = new String(Base64.encodeBase64(policy.getBytes()));
        // 设置policy。
        formFields.put("policy", encodePolicy);
        // 生成签名。
        String signaturecom = com.aliyun.oss.common.auth.ServiceSignature.create().computeSignature(ossProperties.getSecretAccessKey(), encodePolicy);
        // 设置签名。
        formFields.put("Signature", signaturecom);
        String ret = formUpload(urlStr, formFields, localFilePath);
        log.info("Post Object [" + objectName + "] to bucket [" + bucketName + "]");
        log.info("post reponse:" + ret);
        return ret;
    }

    @Override
    public AppendObjectResult appendObjectRequest(String bucketName, String objectName, AppendObjectResult appendObjectResult, InputStream inputStream, ObjectMetadata meta, Callback... callbacks) {
        if (meta == null) {
            meta = new ObjectMetadata();
            // 指定上传的内容类型。
            meta.setContentType("text/plain");
            // 指定该Object的网页缓存行为。
            //meta.setCacheControl("no-cache");
            // 指定该Object被下载时的名称。
            //meta.setContentDisposition("attachment;filename=oss_download.txt");
            // 指定该Object的内容编码格式。
            //meta.setContentEncoding(OSSConstants.DEFAULT_CHARSET_NAME);
            // 该请求头用于检查消息内容是否与发送时一致。
            //meta.setContentMD5("ohhnqLBJFiKkPSBO1eNaUA==");
            // 指定过期时间。
            //try {
            //    meta.setExpirationTime(DateUtil.parseRfc822Date("Wed, 08 Jul 2022 16:57:01 GMT"));
            //} catch (ParseException e) {
            //    e.printStackTrace();
            //}
            // 指定服务器端加密方式。此处指定为OSS完全托管密钥进行加密（SSE-OSS）。
            //meta.setServerSideEncryption(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
            // 指定Object的访问权限。此处指定为私有访问权限。
            //meta.setObjectAcl(CannedAccessControlList.Private);
            // 指定Object的存储类型。
            //meta.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard);
            // 创建AppendObject时可以添加x-oss-meta-*，继续追加时不可以携带此参数。如果配置以x-oss-meta-*为前缀的参数，则该参数视为元数据。
            //meta.setHeader("x-oss-meta-author", "Alice");
        }

        // 通过AppendObjectRequest设置多个参数。
        AppendObjectRequest appendObjectRequest = new AppendObjectRequest(bucketName, objectName, inputStream, meta);

        // 通过AppendObjectRequest设置单个参数。
        // 设置Bucket名称。
        //appendObjectRequest.setBucketName(bucketName);
        // 设置Object名称。
        //appendObjectRequest.setKey(objectName);
        // 设置待追加的内容。可选类型包括InputStream类型和File类型。此处为InputStream类型。
        //appendObjectRequest.setInputStream(new ByteArrayInputStream(content1.getBytes()));
        // 设置待追加的内容。可选类型包括InputStream类型和File类型。此处为File类型。
        //appendObjectRequest.setFile(new File("D:\\localpath\\examplefile.txt"));
        // 指定文件的元信息，第一次追加时有效。
        //appendObjectRequest.setMetadata(meta);

        // 第一次追加。
        // 设置文件的追加位置。
        appendObjectRequest.setPosition(appendObjectResult == null ? 0L : appendObjectResult.getNextPosition());
        // 进度条
        appendObjectRequest.withProgressListener(putObjectProgressListener);
        // 回调函数
        if (ArrayUtils.isNotEmpty(callbacks)) {
            appendObjectRequest.setCallback(callbacks[0]);
        }
        appendObjectResult = ossClient.appendObject(appendObjectRequest);
        // 文件的64位CRC值。此值根据ECMA-182标准计算得出。
        log.info(appendObjectResult.getObjectCRC());
        return appendObjectResult;
    }

    @Override
    public UploadFileResult checkPointUploadFile(String bucketName, String objectName, String localFilePath, ObjectMetadata meta, Callback... callbacks) throws Throwable {
        if (meta == null) {
            meta = new ObjectMetadata();
            // 指定上传的内容类型。
            meta.setContentType("text/plain");

            // 文件上传时设置访问权限ACL。
            // meta.setObjectAcl(CannedAccessControlList.Private);
        }

        // 通过UploadFileRequest设置多个参数。
        // 依次填写Bucket名称（例如examplebucket）以及Object完整路径（例如exampledir/exampleobject.txt），Object完整路径中不能包含Bucket名称。
        UploadFileRequest uploadFileRequest = new UploadFileRequest(bucketName, objectName);

        // 通过UploadFileRequest设置单个参数。
        // 填写本地文件的完整路径，例如D:\\localpath\\examplefile.txt。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
        uploadFileRequest.setUploadFile(localFilePath);
        // 指定上传并发线程数，默认值为1。
        uploadFileRequest.setTaskNum(5);
        // 指定上传的分片大小，单位为字节，取值范围为100 KB~5 GB。默认值为100 KB。
        uploadFileRequest.setPartSize(1024 * 1024);
        // 开启断点续传，默认关闭。
        uploadFileRequest.setEnableCheckpoint(true);
        // 记录本地分片上传结果的文件。上传过程中的进度信息会保存在该文件中，如果某一分片上传失败，再次上传时会根据文件中记录的点继续上传。上传完成后，该文件会被删除。
        // 如果未设置该值，默认与待上传的本地文件同路径，名称为${uploadFile}.ucp。
        //uploadFileRequest.setCheckpointFile("yourCheckpointFile");
        // 文件的元数据。
        uploadFileRequest.setObjectMetadata(meta);
        // 进度条
        uploadFileRequest.withProgressListener(putObjectProgressListener);
        // 回调函数
        if (ArrayUtils.isNotEmpty(callbacks)) {
            uploadFileRequest.setCallback(callbacks[0]);
        }
        // 设置上传回调，参数为Callback类型。
        //uploadFileRequest.setCallback("yourCallbackEvent");

        // 断点续传上传。
        return ossClient.uploadFile(uploadFileRequest);
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(String bucketName, String objectName, File file, ObjectMetadata meta) throws IOException {
        // 创建InitiateMultipartUploadRequest对象。
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);

        // 如果需要在初始化分片时设置请求头，请参考以下示例代码。
        // ObjectMetadata metadata = new ObjectMetadata();
        // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        // 指定该Object的网页缓存行为。
        // metadata.setCacheControl("no-cache");
        // 指定该Object被下载时的名称。
        // metadata.setContentDisposition("attachment;filename=oss_MultipartUpload.txt");
        // 指定该Object的内容编码格式。
        // metadata.setContentEncoding(OSSConstants.DEFAULT_CHARSET_NAME);
        // 指定初始化分片上传时是否覆盖同名Object。此处设置为true，表示禁止覆盖同名Object。
        // metadata.setHeader("x-oss-forbid-overwrite", "true");
        // 指定上传该Object的每个part时使用的服务器端加密方式。
        // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
        // 指定Object的加密算法。如果未指定此选项，表明Object使用AES256加密算法。
        // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_DATA_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
        // 指定KMS托管的用户主密钥。
        // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION_KEY_ID, "9468da86-3509-4f8d-a61e-6eab1eac****");
        // 指定Object的存储类型。
        // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard);
        // 指定Object的对象标签，可同时设置多个标签。
        // metadata.setHeader(OSSHeaders.OSS_TAGGING, "a:1");
        // request.setObjectMetadata(metadata);
        if (meta != null) {
            request.setObjectMetadata(meta);
        }

        // 初始化分片。
        InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
        // 返回uploadId，它是分片上传事件的唯一标识。您可以根据该uploadId发起相关的操作，例如取消分片上传、查询分片上传等。
        String uploadId = upresult.getUploadId();

        // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
        List<PartETag> partETags =  new ArrayList<>();
        // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
        final long partSize = 1024 * 1024L;   //1 MB。

        // 根据上传的数据大小计算分片数。以本地文件为例，说明如何通过File.length()获取上传数据的大小。
        //final File sampleFile = new File("D:\\localpath\\examplefile.txt");
        long fileLength = file.length();
        int partCount = (int) (fileLength / partSize);
        if (fileLength % partSize != 0) {
            partCount++;
        }
        // 遍历分片上传。
        for (int i = 0; i < partCount; i++) {
            long startPos = i * partSize;
            long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(bucketName);
            uploadPartRequest.setKey(objectName);
            uploadPartRequest.setUploadId(uploadId);
            // 设置上传的分片流。
            // 以本地文件为例说明如何创建FIleInputstream，并通过InputStream.skip()方法跳过指定数据。
            InputStream instream = Files.newInputStream(file.toPath());
            instream.skip(startPos);
            uploadPartRequest.setInputStream(instream);
            // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
            uploadPartRequest.setPartSize(curPartSize);
            // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
            uploadPartRequest.setPartNumber( i + 1);
            // 进度条
            uploadPartRequest.withProgressListener(putObjectProgressListener);
            // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
            UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
            // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
            partETags.add(uploadPartResult.getPartETag());
        }


        // 创建CompleteMultipartUploadRequest对象。
        // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);

        // 如果需要在完成分片上传的同时设置文件访问权限，请参考以下示例代码。
        // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.Private);
        // 指定是否列举当前UploadId已上传的所有Part。仅在Java SDK为3.14.0及以上版本时，支持通过服务端List分片数据来合并完整文件时，将CompleteMultipartUploadRequest中的partETags设置为null。
        // Map<String, String> headers = new HashMap<String, String>();
        // 如果指定了x-oss-complete-all:yes，则OSS会列举当前UploadId已上传的所有Part，然后按照PartNumber的序号排序并执行CompleteMultipartUpload操作。
        // 如果指定了x-oss-complete-all:yes，则不允许继续指定body，否则报错。
        // headers.put("x-oss-complete-all","yes");
        // completeMultipartUploadRequest.setHeaders(headers);

        // 完成分片上传。
        CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
        log.info(completeMultipartUploadResult.getETag());
        return completeMultipartUploadResult;
    }

    @Override
    public InputStream getObjectContent(String bucketName, String objectName) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objectName);
        // 进度条
        getObjectRequest.withProgressListener(getObjectProgressListener);
        return ossClient.getObject(getObjectRequest.withProgressListener(getObjectProgressListener)).getObjectContent();
    }

    @Override
    public ObjectMetadata getObject(String bucketName, String objectName, File file) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objectName);
        // 进度条
        getObjectRequest.withProgressListener(getObjectProgressListener);
        return ossClient.getObject(getObjectRequest, file);
    }

    @Override
    public InputStream getObjectContent(String bucketName, String objectName, int rangeStart, int rangeEnd, Boolean rangeBehavior) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objectName);
        // 对于大小为1000 Bytes的文件，正常的字节范围为0~999。
        // 获取0~999字节范围内的数据，包括0和999，共1000个字节的数据。如果指定的范围无效（比如开始或结束位置的指定值为负数，或指定值大于文件大小），则下载整个文件。
        getObjectRequest.setRange(rangeStart, rangeEnd);
        if (rangeBehavior != null && rangeBehavior) {
            // 在请求中增加请求头x-oss-range-behavior:standard，则改变指定范围不在有效区间时OSS的下载行为。假设现有大小为1000 Bytes的Object：
            // 若指定了Range: bytes=500~2000，此时范围末端取值不在有效区间，返回500~999字节范围内容，且HTTP Code为206。
            // 若指定了Range: bytes=1000~2000，此时范围首端取值不在有效区间，返回HTTP Code为416，错误码为InvalidRange。
            getObjectRequest.addHeader("x-oss-range-behavior", "standard");
        }
        // 进度条
        getObjectRequest.withProgressListener(getObjectProgressListener);
        // 范围下载。
        OSSObject ossObject = ossClient.getObject(getObjectRequest);
        return ossObject.getObjectContent();
    }

    @Override
    public DownloadFileResult downloadFile(String bucketName, String objectName, String filePath) throws Throwable {
        // 请求10个任务并发下载。
        DownloadFileRequest downloadFileRequest = new DownloadFileRequest(bucketName, objectName);
        // 指定Object下载到本地文件的完整路径，例如D:\\localpath\\examplefile.txt。
        downloadFileRequest.setDownloadFile(filePath);
        // 设置分片大小，单位为字节，取值范围为100 KB~5 GB。默认值为100 KB。
        downloadFileRequest.setPartSize(1024 * 1024);
        // 设置分片下载的并发数，默认值为1。
        downloadFileRequest.setTaskNum(10);
        // 开启断点续传下载，默认关闭。
        downloadFileRequest.setEnableCheckpoint(true);
        // 设置断点记录文件的完整路径，例如D:\\localpath\\examplefile.txt.dcp。
        // 只有当Object下载中断产生了断点记录文件后，如果需要继续下载该Object，才需要设置对应的断点记录文件。下载完成后，该文件会被删除。
        downloadFileRequest.setCheckpointFile(filePath + ".dcp");

        // 进度条
        downloadFileRequest.withProgressListener(getObjectProgressListener);

        // 下载文件。
        DownloadFileResult downloadRes = ossClient.downloadFile(downloadFileRequest);
        // 下载成功时，会返回文件元信息。
        ObjectMetadata objectMetadata = downloadRes.getObjectMetadata();
        log.info(objectMetadata.getETag());
        log.info(objectMetadata.getLastModified().toString());
        log.info(objectMetadata.getUserMetadata().get("meta"));
        return downloadRes;
    }

    @Override
    public ObjectMetadata getObject(String bucketName, String objectName, File file, Date modifiedSince, Date unmodifiedSince, Boolean match) {
        GetObjectRequest request = new GetObjectRequest(bucketName, objectName);
        // 设置限定条件。
        if (modifiedSince != null) {
            request.setModifiedSinceConstraint(modifiedSince);
        }
        if (unmodifiedSince != null) {
            request.setUnmodifiedSinceConstraint(unmodifiedSince);
        }
        /*if (match != null && match) {
            request.setMatchingETagConstraints(Collections.singletonList(ossClient.getObjectMetadata(request).getETag()));
        }
        if (match != null && !match) {
            request.setNonmatchingETagConstraints(Collections.singletonList(ossClient.getObjectMetadata(request).getETag()));
        }*/

        // 进度条
        request.withProgressListener(getObjectProgressListener);
        // 下载OSS文件到本地文件。
        return ossClient.getObject(request, file);
    }

    @Override
    public List<OSSObjectSummary> getObjectSummaries(String bucketName) {
        return ossClient.listObjects(bucketName).getObjectSummaries();
    }

    @Override
    public VoidResult deleteObject(String bucketName, String objectName) {
        return ossClient.deleteObject(bucketName, objectName);
    }

    @Override
    public URL generatorUrl(String bucketName, String objectName, Date expire) {
        return ossClient.generatePresignedUrl(bucketName, objectName, expire);
    }

    @Override
    public AccessControlList getBucketAcl(String bucketName) {
        return ossClient.getBucketAcl(bucketName);
    }

    @Override
    public VoidResult setBucketAcl(String bucketName, CannedAccessControlList acl) {
        return ossClient.setBucketAcl(bucketName, acl);
    }

    @Override
    public ObjectAcl getObjectAcl(String bucketName, String objectName) {
        return ossClient.getObjectAcl(bucketName, objectName);
    }

    @Override
    public VoidResult setObjectAcl(String bucketName, String objectName, CannedAccessControlList acl) {
        return ossClient.setObjectAcl(bucketName, objectName, acl);
    }

    private static String formUpload(String urlStr, Map<String, String> formFields, String localFile)
            throws Exception {
        String res = "";
        HttpURLConnection conn = null;
        String boundary = "9431149156168";
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            // 设置MD5值。MD5值由整个Body计算得出。如果希望开启MD5校验，可参考MD5加密设置。
            // conn.setRequestProperty("Content-MD5", contentMD5);
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            // 遍历读取表单Map中的数据，将数据写入到输出流中。
            if (formFields != null) {
                StringBuffer strBuf = new StringBuffer();
                Iterator<Map.Entry<String, String>> iter = formFields.entrySet().iterator();
                int i = 0;
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    String inputName = entry.getKey();
                    String inputValue = entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    if (i == 0) {
                        strBuf.append("--").append(boundary).append("\r\n");
                        strBuf.append("Content-Disposition: form-data; name=\""
                                + inputName + "\"\r\n\r\n");
                        strBuf.append(inputValue);
                    } else {
                        strBuf.append("\r\n").append("--").append(boundary).append("\r\n");
                        strBuf.append("Content-Disposition: form-data; name=\""
                                + inputName + "\"\r\n\r\n");
                        strBuf.append(inputValue);
                    }
                    i++;
                }
                out.write(strBuf.toString().getBytes());
            }
            // 读取文件信息，将要上传的文件写入到输出流中。
            File file = new File(localFile);
            String filename = file.getName();
            String contentType = new MimetypesFileTypeMap().getContentType(file);
            if (contentType == null || contentType.equals("")) {
                contentType = "application/octet-stream";
            }
            StringBuffer strBuf = new StringBuffer();
            strBuf.append("\r\n").append("--").append(boundary)
                    .append("\r\n");
            strBuf.append("Content-Disposition: form-data; name=\"file\"; "
                    + "filename=\"" + filename + "\"\r\n");
            strBuf.append("Content-Type: " + contentType + "\r\n\r\n");
            out.write(strBuf.toString().getBytes());
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            int bytes = 0;
            byte[] bufferOut = new byte[1024];
            while ((bytes = in.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }
            in.close();
            byte[] endData = ("\r\n--" + boundary + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();
            // 读取返回数据。
            strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            res = strBuf.toString();
            reader.close();
            reader = null;
        } catch (ClientException e) {
            System.err.println("Send post request exception: " + e);
            System.err.println(e.getErrorCode()+" msg="+e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        return res;
    }
    private static void setCallBack(Map<String, String> formFields, Callback callback) {
        if (callback != null) {
            String jsonCb = OSSUtils.jsonizeCallback(callback);
            String base64Cb = BinaryUtil.toBase64String(jsonCb.getBytes());
            formFields.put("callback", base64Cb);
            if (callback.hasCallbackVar()) {
                Map<String, String> varMap = callback.getCallbackVar();
                formFields.putAll(varMap);
            }
        }
    }
}
