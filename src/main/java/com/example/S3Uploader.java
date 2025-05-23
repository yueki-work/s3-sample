package com.example;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

public class S3Uploader {
    public static void main(String[] args) throws IOException {
        // S3接続情報（LocalStack用のデフォルト値）
        String endpoint = "http://localhost:4566";
        String region = "us-east-1";
        String accessKey = "test";
        String secretKey = "test";
        String bucketName = "sample-bucket";
        String key = "dummy.txt";

        // ダミーファイル作成
        File dummyFile = new File("dummy.txt");
        try (FileWriter writer = new FileWriter(dummyFile)) {
            writer.write("This is a dummy file for S3 upload sample.");
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

        // バケット作成（存在しない場合）
        ListBucketsResponse listBucketsResponse = s3.listBuckets();
        boolean exists = listBucketsResponse.buckets().stream()
                .anyMatch(b -> b.name().equals(bucketName));
        if (!exists) {
            s3.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            System.out.println("Bucket created: " + bucketName);
        }

        // ファイルアップロード
        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromFile(dummyFile));
        System.out.println("File uploaded: " + key);

        // クリーンアップ
        dummyFile.delete();
    }
}
