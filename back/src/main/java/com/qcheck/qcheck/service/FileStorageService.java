package com.qcheck.qcheck.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 파일 저장 서비스
 *
 * 책임:
 * - 불량 이미지 파일 저장
 * - 파일 로드 및 조회
 * - 파일 유효성 검증 (확장자, 크기)
 *
 * 저장 경로:
 * - {upload-dir}/defects/{company_seq}/{yyyy-MM}/
 * - 예: /files/defects/1/2026-05/abc123-def456.jpg
 *
 * 검증:
 * - 허용 확장자: jpg, jpeg, png, gif, bmp
 * - 최대 크기: 10MB
 *
 * 파일명:
 * - UUID + 원본 확장자
 * - 예: a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
 */
@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir:/files/defects}")
    private String uploadDir;

    /**
     * 허용된 파일 확장자 (소문자)
     */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>();
    static {
        ALLOWED_EXTENSIONS.add("jpg");
        ALLOWED_EXTENSIONS.add("jpeg");
        ALLOWED_EXTENSIONS.add("png");
        ALLOWED_EXTENSIONS.add("gif");
        ALLOWED_EXTENSIONS.add("bmp");
    }

    /**
     * 최대 파일 크기 (10MB)
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 파일 저장
     *
     * 프로세스:
     * 1. 파일 유효성 검증 (null 체크, 크기, 확장자)
     * 2. 회사별/월별 디렉토리 생성
     * 3. UUID + 원본 확장자로 파일명 생성
     * 4. 파일 저장
     * 5. 상대 경로 반환 (데이터베이스 저장용)
     *
     * @param companySeq 회사 ID
     * @param file 업로드 파일
     * @return 상대 경로 (예: /files/defects/1/2026-05/abc123.jpg)
     * @throws RuntimeException 파일 검증 실패 또는 저장 실패 시
     */
    public String storeFile(Long companySeq, MultipartFile file) {
        // 파일 null 체크
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("업로드된 파일이 없습니다");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("파일 크기가 10MB를 초과합니다");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new RuntimeException("유효한 파일명이 아닙니다");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("허용되지 않는 파일 형식입니다 (jpg, jpeg, png, gif, bmp만 가능)");
        }

        try {
            // 회사별/월별 디렉토리 생성
            LocalDate today = LocalDate.now();
            String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String dirPath = String.format("%s/%d/%s", uploadDir, companySeq, yearMonth);
            Path directory = Paths.get(dirPath);
            Files.createDirectories(directory);

            // UUID + 확장자로 파일명 생성
            String fileName = UUID.randomUUID() + "." + extension;
            Path filePath = directory.resolve(fileName);

            // 파일 저장
            file.transferTo(filePath);

            // 상대 경로 반환
            String relativePath = String.format("/files/defects/%d/%s/%s", companySeq, yearMonth, fileName);
            log.info("File stored: companySeq={}, path={}, size={}", companySeq, relativePath, file.getSize());
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to store file: companySeq={}, filename={}", companySeq, originalFilename, e);
            throw new RuntimeException("파일 저장에 실패했습니다", e);
        }
    }

    /**
     * 파일 로드
     *
     * @param imagePath 이미지 경로 (예: /files/defects/1/2026-05/abc123.jpg)
     * @return Resource 객체 (Spring이 파일 다운로드 처리)
     * @throws RuntimeException 파일을 찾을 수 없으면 예외 발생
     */
    public Resource loadFileAsResource(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            throw new RuntimeException("이미지 경로가 없습니다");
        }

        try {
            // imagePath가 /files/defects/...로 시작하면 uploadDir 기준 경로로 변환
            String relativePath = imagePath.startsWith("/files/") ? imagePath.substring(1) : imagePath;
            Path filePath = Paths.get(uploadDir).getParent().resolve(relativePath);

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                log.debug("File loaded: {}", imagePath);
                return resource;
            } else {
                throw new RuntimeException("파일을 찾을 수 없습니다: " + imagePath);
            }
        } catch (Exception e) {
            log.error("Failed to load file: {}", imagePath, e);
            throw new RuntimeException("파일 로드에 실패했습니다", e);
        }
    }

    /**
     * 파일 삭제
     *
     * @param imagePath 이미지 경로
     * @throws RuntimeException 파일 삭제 실패 시
     */
    public void deleteFile(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }

        try {
            String relativePath = imagePath.startsWith("/files/") ? imagePath.substring(1) : imagePath;
            Path filePath = Paths.get(uploadDir).getParent().resolve(relativePath);
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", imagePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", imagePath, e);
            throw new RuntimeException("파일 삭제에 실패했습니다", e);
        }
    }
}
