package com.example;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.http.apache.ApacheHttpClient;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class S3UploaderTest {

    @Container
    public static LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.3.0"))
            .withServices(LocalStackContainer.Service.S3);

    @Test
    void testS3Upload() throws Exception {
        String region = localstack.getRegion();
        URI endpoint = localstack.getEndpointOverride(LocalStackContainer.Service.S3);
        String accessKey = localstack.getAccessKey();
        String secretKey = localstack.getSecretKey();
        String bucketName = "test-bucket";
        String key = "test.txt";

        // ダミーファイル作成
        File dummyFile = new File("test.txt");
        File downloaded = File.createTempFile("downloaded-", ".txt");
        String fileContent = "Hello from test!";
        
        // First write the content
        try (FileWriter writer = new FileWriter(dummyFile)) {
            writer.write(fileContent);
        }
        
        // Then do the S3 operations
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(endpoint)
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .httpClient(ApacheHttpClient.builder().build())
                .forcePathStyle(true)
                .build()) {
                     
            // バケット作成
            s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    
            // ファイルアップロード
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    RequestBody.fromFile(dummyFile)
            );
    
            // S3からダウンロードして内容検証
            ResponseInputStream<GetObjectResponse> objectData = s3.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build()
            );
            
            try (InputStream in = objectData;
                 java.io.OutputStream out = Files.newOutputStream(downloaded.toPath())) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
            
            String downloadedContent = new String(Files.readAllBytes(downloaded.toPath()), StandardCharsets.UTF_8);
            assertEquals(fileContent, downloadedContent);
            
        } finally {
            // クリーンアップ
            dummyFile.delete();
            downloaded.delete();
        }
    }
}