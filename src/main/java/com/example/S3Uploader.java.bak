package com.example;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

        // S3クライアント作成
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withPathStyleAccessEnabled(true)
                .build();

        // バケット作成（存在しない場合）
        if (!s3.doesBucketExistV2(bucketName)) {
            s3.createBucket(bucketName);
            System.out.println("Bucket created: " + bucketName);
        }

        // ファイルアップロード
        s3.putObject(bucketName, key, dummyFile);
        System.out.println("File uploaded: " + key);

        // クリーンアップ
        dummyFile.delete();
    }
}
