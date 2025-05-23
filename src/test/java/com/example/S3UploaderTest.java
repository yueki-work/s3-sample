package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.S3Configuration;
import java.net.URI;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;


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
        String endpoint = localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString();
        String accessKey = localstack.getAccessKey();
        String secretKey = localstack.getSecretKey();
        String bucketName = "test-bucket";
        String key = "test.txt";

        // ダミーファイル作成
        File dummyFile = new File("test.txt");
        String fileContent = "Hello from test!";
        try (FileWriter writer = new FileWriter(dummyFile)) {
            writer.write(fileContent);
        }

         // S3クライアント作成（AWS SDK v2）
        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();

         // バケット作成
        s3.createBucket(CreateBucketRequest.builder()
                .bucket(bucketName)
                .build());

         // ファイルアップロード
        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromFile(dummyFile));

         // S3からダウンロードして内容検証
        ResponseBytes<GetObjectResponse> responseBytes = s3.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(), ResponseTransformer.toBytes());
        String downloadedContent = new String(responseBytes.asByteArray(), StandardCharsets.UTF_8);
        assertEquals(fileContent, downloadedContent);

        // クリーンアップ
        dummyFile.delete();

    }
}
