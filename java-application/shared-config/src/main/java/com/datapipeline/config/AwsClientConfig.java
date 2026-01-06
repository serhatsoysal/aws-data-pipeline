package com.datapipeline.config;

import java.time.Duration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsUtilities;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

public class AwsClientConfig {

    private static volatile S3Client s3Client;
    private static volatile S3AsyncClient s3AsyncClient;
    private static volatile S3TransferManager transferManager;
    private static volatile SnsClient snsClient;
    private static volatile SqsClient sqsClient;
    private static volatile RdsUtilities rdsUtilities;

    private static final Region AWS_REGION = Region.of(EnvironmentConfig.getAwsRegion());

    public static S3Client getS3Client() {
        if (s3Client == null) {
            synchronized (AwsClientConfig.class) {
                if (s3Client == null) {
                    s3Client = S3Client.builder()
                            .region(AWS_REGION)
                            .credentialsProvider(DefaultCredentialsProvider.create())
                            .httpClientBuilder(ApacheHttpClient.builder()
                                    .connectionTimeout(Duration.ofSeconds(30))
                                    .socketTimeout(Duration.ofSeconds(30)))
                            .overrideConfiguration(config -> config
                                    .retryPolicy(RetryPolicy.builder()
                                            .numRetries(3)
                                            .build()))
                            .build();
                }
            }
        }
        return s3Client;
    }

    public static S3AsyncClient getS3AsyncClient() {
        if (s3AsyncClient == null) {
            synchronized (AwsClientConfig.class) {
                if (s3AsyncClient == null) {
                    s3AsyncClient = S3AsyncClient.builder()
                            .region(AWS_REGION)
                            .credentialsProvider(DefaultCredentialsProvider.create())
                            .build();
                }
            }
        }
        return s3AsyncClient;
    }

    public static S3TransferManager getTransferManager() {
        if (transferManager == null) {
            synchronized (AwsClientConfig.class) {
                if (transferManager == null) {
                    transferManager = S3TransferManager.builder()
                            .s3Client(getS3AsyncClient())
                            .build();
                }
            }
        }
        return transferManager;
    }

    public static SnsClient getSnsClient() {
        if (snsClient == null) {
            synchronized (AwsClientConfig.class) {
                if (snsClient == null) {
                    snsClient = SnsClient.builder()
                            .region(AWS_REGION)
                            .credentialsProvider(DefaultCredentialsProvider.create())
                            .httpClientBuilder(ApacheHttpClient.builder()
                                    .connectionTimeout(Duration.ofSeconds(30))
                                    .socketTimeout(Duration.ofSeconds(30)))
                            .overrideConfiguration(config -> config
                                    .retryPolicy(RetryPolicy.builder()
                                            .numRetries(3)
                                            .build()))
                            .build();
                }
            }
        }
        return snsClient;
    }

    public static SqsClient getSqsClient() {
        if (sqsClient == null) {
            synchronized (AwsClientConfig.class) {
                if (sqsClient == null) {
                    sqsClient = SqsClient.builder()
                            .region(AWS_REGION)
                            .credentialsProvider(DefaultCredentialsProvider.create())
                            .httpClientBuilder(ApacheHttpClient.builder()
                                    .connectionTimeout(Duration.ofSeconds(30))
                                    .socketTimeout(Duration.ofSeconds(30)))
                            .overrideConfiguration(config -> config
                                    .retryPolicy(RetryPolicy.builder()
                                            .numRetries(3)
                                            .build()))
                            .build();
                }
            }
        }
        return sqsClient;
    }

    public static RdsUtilities getRdsUtilities() {
        if (rdsUtilities == null) {
            synchronized (AwsClientConfig.class) {
                if (rdsUtilities == null) {
                    rdsUtilities = RdsUtilities.builder()
                            .region(AWS_REGION)
                            .build();
                }
            }
        }
        return rdsUtilities;
    }

    public static void closeAll() {
        if (s3Client != null) {
            s3Client.close();
        }
        if (s3AsyncClient != null) {
            s3AsyncClient.close();
        }
        if (transferManager != null) {
            transferManager.close();
        }
        if (snsClient != null) {
            snsClient.close();
        }
        if (sqsClient != null) {
            sqsClient.close();
        }
    }
}

