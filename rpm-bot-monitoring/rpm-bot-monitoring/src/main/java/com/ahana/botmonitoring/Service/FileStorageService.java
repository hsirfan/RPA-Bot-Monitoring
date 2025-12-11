
package com.ahana.botmonitoring.Service;

import com.ahana.botmonitoring.Exception.FileStorageException;
import com.ahana.botmonitoring.Exception.MyFileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class FileStorageService {

    private Path fileStorageLocation;

    @Value("${file.upload-dir}")
    private String uploadPath;

    public String storeFile(MultipartFile file,
            String botName,
            String processName,
            String processVersion) {

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = Paths.get(originalFileName).getFileName().toString();

        this.fileStorageLocation = Paths.get(uploadPath, botName, processName, processVersion)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create upload directory", ex);
        }

        try {
            if (fileName.contains("..")) {
                throw new FileStorageException("Invalid path sequence in file name: " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName, ex);
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + filePath);
            }

        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + filePath, ex);
        }
    }

    public Path getFileStorageLocation() {
        return this.fileStorageLocation;
    }
}
