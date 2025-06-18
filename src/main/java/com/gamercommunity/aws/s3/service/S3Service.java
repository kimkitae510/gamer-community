package com.gamercommunity.aws.s3.service;

import com.gamercommunity.global.exception.custom.FileUploadException;
import com.gamercommunity.global.exception.custom.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    //허용 가능한 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    //최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;


    //파일 업로드
    public String uploadFile(MultipartFile file, String dirName) {
        validateFile(file);

        String filename = generateFilename(file.getOriginalFilename(), dirName);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            String uploadedUrl = getFileUrl(filename);
            log.info("S3 파일 업로드 성공: {}", uploadedUrl);
            return uploadedUrl;

        } catch (IOException e) {
            log.error("S3 파일 업로드 실패: {}", e.getMessage());
            throw new FileUploadException("파일 업로드 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("S3 업로드 처리 실패: {}", e.getMessage());
            throw new FileUploadException("S3 업로드 처리 중 오류가 발생했습니다.", e);
        }
    }


    //파일 삭제
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            log.warn("삭제할 파일 URL이 없습니다.");
            return;
        }

        if (!isS3Url(fileUrl)) {
            log.warn("S3 URL이 아닙니다: {}", fileUrl);
            return;
        }

        try {
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 파일 삭제 성공: {}", key);

        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", e.getMessage());
            throw new FileUploadException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

    //파일 검증
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("업로드할 파일이 없습니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidRequestException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidRequestException("파일 이름이 유효하지 않습니다.");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidRequestException("허용되지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
        }
    }


    //파일 이름 생성
    private String generateFilename(String originalFilename, String dirName) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return dirName + "/" + uuid + "." + extension;
    }


    //파일 확장자 추출
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new InvalidRequestException("파일 확장자가 없습니다.");
        }
        return filename.substring(lastDotIndex + 1);
    }

    //S3 파일 URL 생성
    private String getFileUrl(String key) {
        return s3Client.utilities()
                .getUrl(builder -> builder.bucket(bucketName).key(key))
                .toExternalForm();
    }


    //S3 URL 여부 확인
    private boolean isS3Url(String fileUrl) {
        return fileUrl.startsWith("s3://") ||
                fileUrl.contains(bucketName + ".s3.") ||
                fileUrl.contains(".amazonaws.com/");
    }

    //URL에서 S3 Key 추출
    private String extractKeyFromUrl(String fileUrl) {
        String key;

        if (fileUrl.startsWith("s3://")) {
            int idx = fileUrl.indexOf(bucketName);
            if (idx < 0) {
                throw new InvalidRequestException("S3 URL에 버킷 이름이 없습니다: " + fileUrl);
            }
            key = fileUrl.substring(idx + bucketName.length() + 1);
        } else {
            String httpsPrefix = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
            String httpPrefix = "http://" + bucketName + ".s3." + region + ".amazonaws.com/";

            if (fileUrl.startsWith(httpsPrefix)) {
                key = fileUrl.substring(httpsPrefix.length());
            } else if (fileUrl.startsWith(httpPrefix)) {
                key = fileUrl.substring(httpPrefix.length());
            } else {
                throw new InvalidRequestException("올바른 S3 URL 형식이 아닙니다: " + fileUrl);
            }
        }

        //URL 디코딩
        return URLDecoder.decode(key, StandardCharsets.UTF_8);
    }
}
