package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.File;
import java.io.FileWriter;
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

        // S3クライアント作成
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withPathStyleAccessEnabled(true)
                .build();

        // バケット作成
        s3.createBucket(bucketName);

        // ファイルアップロード
        s3.putObject(bucketName, key, dummyFile);

        // S3からダウンロードして内容検証
        File downloaded = File.createTempFile("downloaded-", ".txt");
        try (java.io.InputStream in = s3.getObject(bucketName, key).getObjectContent();
             java.io.OutputStream out = Files.newOutputStream(downloaded.toPath())) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        String downloadedContent = new String(Files.readAllBytes(downloaded.toPath()), StandardCharsets.UTF_8);
        assertEquals(fileContent, downloadedContent);

        // クリーンアップ
        dummyFile.delete();
        downloaded.delete();
    }
}
