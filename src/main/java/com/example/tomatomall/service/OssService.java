package com.example.tomatomall.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class OssService {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    public String uploadFile(byte[] fileContent, String fileName) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 上传文件
            ossClient.putObject(bucketName, fileName, new ByteArrayInputStream(fileContent));

            // 返回文件访问路径
            return "https://" + bucketName + "." + endpoint + "/" + fileName;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}