package com.ahana.botmonitoring.Controller;

import com.ahana.botmonitoring.Service.ChecksumService;
import com.ahana.botmonitoring.generated.api.ChecksumControllerApi;
import com.ahana.botmonitoring.generated.model.UploadFileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin(origins = "${app.cors.allowed-origin}")
public class ChecksumController implements ChecksumControllerApi {

    private final ChecksumService checksumService;

    public ChecksumController(ChecksumService checksumService) {
        this.checksumService = checksumService;
    }

    @Override
    public ResponseEntity<UploadFileResponse> uploadFile(MultipartFile file, String processName, String processVersion,
            String botName) {
        UploadFileResponse response = checksumService.uploadSingleFile(file, processName, processVersion, botName);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<UploadFileResponse>> uploadMultipleFiles(String processName, String processVersion,
            String botName, List<Resource> files) {
        log.warn(
                "uploadMultipleFiles called with List<Resource> - this may need OpenAPI spec update to use MultipartFile[]");
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PutMapping("/uploadXamlFiles/{processName}/{processVersion}/{botName}")
    public List<UploadFileResponse> uploadMultipleFilesLegacy(@PathVariable String processName,
            @PathVariable String processVersion,
            @PathVariable String botName,
            @RequestParam("files") MultipartFile[] files) {
        return checksumService.uploadMultipleFiles(files, processName, processVersion, botName);
    }

    @Override
    public ResponseEntity<String> validateChecksum(String machineName, String processName, String processVersion,
            String checksumId, String fileName) {
        try {
            String result = checksumService.validateChecksum(machineName, processName, processVersion, checksumId,
                    fileName);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<Object>> getAllChecksumData() {
        List<Map<String, Object>> data = checksumService.getAllChecksumData();

        // Convert to List<Object> as required by the API interface
        List<Object> result = data.stream()
                .map(map -> (Object) map)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
