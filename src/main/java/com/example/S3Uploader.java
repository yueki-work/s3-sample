package com.example;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

public class S3Uploader {
    public static void main(String[] args) throws IOException {
        // S3接続情報（LocalStack用のデフォルト値）
        String endpoint = "http://localhost:4566";
        String regionStr = "us-east-1";
        Region region = Region.of(regionStr);
        String accessKey = "test";
        String secretKey = "test";
        String bucketName = "sample-bucket";
        String key = "dummy.txt";

        // ダミーファイル作成
        File dummyFile = new File("dummy.txt");
        try (FileWriter writer = new FileWriter(dummyFile)) {
            writer.write("This is a dummy file for S3 upload sample.");
        }

        // S3クライアント作成
        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true)
                .build();

        // バケット作成（存在しない場合）
        if (!bucketExists(s3, bucketName)) {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            System.out.println("Bucket created: " + bucketName);
        }

        // ファイルアップロード
        s3.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(), 
                dummyFile.toPath());
        System.out.println("File uploaded: " + key);

        // クリーンアップ
        dummyFile.delete();
        s3.close(); // Close the client when finished
    }
    
    // Check if bucket exists
    private static boolean bucketExists(S3Client s3Client, String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }
}
