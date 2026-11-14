package com.shipping.auth_service.Service;

import org.springframework.core.io.Resource;
import com.shipping.auth_service.Exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> EXT = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "application/pdf", ".pdf");

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String dir) throws IOException {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "File is required");
        if (file.getSize() > MAX_BYTES)
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "File must be 5 MB or smaller");
        String ext = EXT.get(file.getContentType());
        if (ext == null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only JPG, PNG or PDF files are allowed");

        String key = UUID.randomUUID() + ext;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(key));
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file");
        }
        return key;
    }

    public FileSystemResource load(String key) {
        Path p = root.resolve(key).normalize();
        if (!p.startsWith(root) || !Files.exists(p))
            throw new ApiException(HttpStatus.NOT_FOUND, "File not found");
        return new FileSystemResource(p);
    }

    public void delete(String key) {
        try {
            Path p = root.resolve(key).normalize();
            if (p.startsWith(root)) Files.deleteIfExists(p);
        } catch (IOException ignored) { }
    }
}
