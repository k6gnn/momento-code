package com.momento.service;

import com.momento.exception.S3StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Service
public class S3Service {
    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final String bucket;
    private final int presignedMinutes;
    private final S3Client s3Client;
    private final S3Presigner presigner;

    public S3Service(@Value("${app.aws.region}") String region,
                     @Value("${app.aws.bucket}") String bucket,
                     @Value("${app.aws.access-key-id}") String accessKey,
                     @Value("${app.aws.secret-access-key}") String secretKey,
                     @Value("${app.aws.endpoint:#{null}}") String endpoint,
                     @Value("${app.aws.presigned-url-minutes}") int presignedMinutes) {
        var creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        this.bucket = bucket;
        this.presignedMinutes = presignedMinutes;

        var overrideConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofSeconds(15))
                .apiCallAttemptTimeout(Duration.ofSeconds(8))
                .build();

        var s3Builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(creds)
                .overrideConfiguration(overrideConfig);
        var presignBuilder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(creds);

        if (endpoint != null && !endpoint.isBlank()) {
            s3Builder.endpointOverride(java.net.URI.create(endpoint));
            presignBuilder.endpointOverride(java.net.URI.create(endpoint));
        }

        this.s3Client = s3Builder.build();
        this.presigner = presignBuilder.build();
    }

    public void uploadBytes(String key, byte[] bytes, String contentType) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build(),
                    RequestBody.fromBytes(bytes));
        } catch (SdkException e) {
            log.error("S3 upload failed for key {}: {}", key, e.getMessage());
            throw new S3StorageException("Failed to upload media to storage", e);
        }
    }

    public URL generateSignedGetUrl(String key) {
        try {
            var get = GetObjectRequest.builder().bucket(bucket).key(key).build();
            return presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(presignedMinutes))
                            .getObjectRequest(get)
                            .build())
                    .url();
        } catch (SdkException e) {
            log.error("S3 presign failed for key {}: {}", key, e.getMessage());
            throw new S3StorageException("Failed to generate media URL", e);
        }
    }

    public void deleteObject(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (SdkException e) {
            log.error("S3 delete failed for key {}: {}", key, e.getMessage());
            throw new S3StorageException("Failed to delete media from storage", e);
        }
    }
}
