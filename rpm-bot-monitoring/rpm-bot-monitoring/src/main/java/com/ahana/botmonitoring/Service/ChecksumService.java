package com.ahana.botmonitoring.Service;

import com.ahana.botmonitoring.Entity.BotModel;
import com.ahana.botmonitoring.Entity.Checksum;
import com.ahana.botmonitoring.Repository.ChecksumRepository;
import com.ahana.botmonitoring.generated.model.UploadFileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChecksumService {

        private final ChecksumRepository checksumRepository;
        private final FileStorageService fileStorageService;
        private final BotService botService;
        private final MongoTemplate mongoTemplate;

        public ChecksumService(ChecksumRepository checksumRepository,
                        FileStorageService fileStorageService,
                        BotService botService,
                        MongoTemplate mongoTemplate) {
                this.checksumRepository = checksumRepository;
                this.fileStorageService = fileStorageService;
                this.botService = botService;
                this.mongoTemplate = mongoTemplate;
        }

        private String generateChecksum(MultipartFile file) throws Exception {
                MessageDigest mdigest = MessageDigest.getInstance("SHA-1");
                try (InputStream is = file.getInputStream()) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                                mdigest.update(buffer, 0, bytesRead);
                        }
                }

                StringBuilder sb = new StringBuilder();
                for (byte b : mdigest.digest()) {
                        sb.append(String.format("%02X", b));
                }
                return sb.toString();
        }

        public UploadFileResponse uploadSingleFile(MultipartFile file, String processName, String processVersion,
                        String botName) {
                try {
                        // Store file physically
                        String filePath = fileStorageService.storeFile(file, botName, processName, processVersion);
                        String fileName = Path.of(filePath).getFileName().toString();

                        // Generate checksum
                        String checksum = generateChecksum(file);

                        // Get bot details
                        com.ahana.botmonitoring.generated.model.BotDTO bot = botService.getBotByName(botName)
                                        .orElseThrow(() -> new RuntimeException("Bot not found: " + botName));
                        String machineName = bot.getMachineName();

                        // Get botId from BotModel
                        Query botQuery = new Query(Criteria.where("botName").is(botName));
                        BotModel botModel = mongoTemplate.findOne(botQuery, BotModel.class);
                        Long botId = botModel != null && botModel.getBotId() != null ? botModel.getBotId().longValue()
                                        : null;

                        // Check for existing checksum record
                        Optional<Checksum> existing = botId != null
                                        ? checksumRepository.findByProcessNameAndBotId(processName, botId)
                                        : checksumRepository.findByProcessNameAndBotName(processName, botName);

                        Checksum model = existing.orElse(new Checksum());

                        // Set checksum properties
                        if (botId != null) {
                                model.setBotId(botId);
                        }
                        model.setBotName(botName);
                        model.setMachineName(machineName);
                        model.setProcessName(processName);
                        model.setProcessVersion(processVersion);
                        model.setChecksum(checksum);
                        model.setFileuri(filePath);
                        model.setUploadedAt(LocalDateTime.now());

                        // Save to database
                        checksumRepository.save(model);

                        // Build download URL
                        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                        .path("/downloadFile/")
                                        .path(fileName)
                                        .toUriString();

                        return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());

                } catch (Exception e) {
                        log.error("Error while saving file: {}", e.getMessage(), e);
                        throw new RuntimeException("Error while saving file", e);
                }
        }

        public List<UploadFileResponse> uploadMultipleFiles(MultipartFile[] files, String processName,
                        String processVersion, String botName) {
                return Arrays.stream(files)
                                .map(file -> saveSingleFile(file, processName, processVersion, botName))
                                .collect(Collectors.toList());
        }

        public String validateChecksum(String machineName, String processName, String processVersion, String checksumId,
                        String fileName) {
                // Validate required params
                if (isEmpty(machineName) || isEmpty(processName) ||
                                isEmpty(processVersion) || isEmpty(checksumId) || isEmpty(fileName)) {
                        throw new IllegalArgumentException("Error: All parameters are required");
                }

                // Regex to check that fileuri ENDS WITH (filename)
                String fileNameRegex = fileName + "$"; // Example: "Main.xaml$"

                // Call single MongoDB query
                Optional<Checksum> result = checksumRepository.findByAllFields(
                                machineName, // ?0
                                processName, // ?1
                                processVersion, // ?2
                                checksumId, // ?3
                                fileNameRegex // ?4
                );

                // Return result
                return result.isPresent() ? "Matched" : "Un-matched";
        }

        public List<Map<String, Object>> getAllChecksumData() {
                List<Checksum> list = checksumRepository.findAll();

                // Group by bot + machine + processName + version
                Map<String, List<Checksum>> grouped = list.stream()
                                .collect(Collectors.groupingBy(c -> c.getBotName() + "|" +
                                                c.getMachineName() + "|" +
                                                c.getProcessName() + "|" +
                                                c.getProcessVersion()));

                // Build final response
                List<Map<String, Object>> finalResponse = new ArrayList<>();

                for (String key : grouped.keySet()) {
                        List<Checksum> checksumList = grouped.get(key);

                        String[] parts = key.split("\\|");

                        Map<String, Object> groupMap = new LinkedHashMap<>();
                        groupMap.put("botName", parts[0]);
                        groupMap.put("machineName", parts[1]);
                        groupMap.put("processName", parts[2]);
                        groupMap.put("processVersion", parts[3]);

                        // Prepare file list
                        List<Map<String, Object>> files = checksumList.stream().map(c -> {
                                Map<String, Object> f = new LinkedHashMap<>();
                                f.put("checksumId", c.getChecksum());
                                f.put("fileName", Paths.get(c.getFileuri()).getFileName().toString());
                                f.put("lastModified", c.getUploadedAt());
                                return f;
                        }).collect(Collectors.toList());

                        groupMap.put("files", files);

                        finalResponse.add(groupMap);
                }

                return finalResponse;
        }

        private boolean isEmpty(String value) {
                return value == null || value.trim().isEmpty();
        }


        private UploadFileResponse saveSingleFile(MultipartFile file,
                                                  String processName,
                                                  String processVersion,
                                                  String botName) {

                try {
                        // Store file physically
                        String filePath = fileStorageService.storeFile(file, botName, processName, processVersion);
                        String fileName = Path.of(filePath).getFileName().toString();

                        // Generate checksum
                        String checksum = generateChecksum(file);

                        com.ahana.botmonitoring.generated.model.BotDTO bot = botService.getBotByName(botName)
                                .orElseThrow(() -> new RuntimeException("Bot not found: " + botName));
                        String machineName = bot.getMachineName();

                        // Get machine name
                        // BotModel bot = botRepository.findById(botName)
                        //      .orElseThrow(() -> new RuntimeException("Bot not found: " + botName));

                        // Create new record ALWAYS
                        Checksum model = new Checksum();
                        model.setBotName(botName);
                        model.setMachineName(bot.getMachineName());
                        model.setProcessName(processName);
                        model.setProcessVersion(processVersion);
                        model.setChecksum(checksum);
                        model.setFileuri(filePath);
                        model.setUploadedAt(LocalDateTime.now());

                        checksumRepository.save(model);

                        // Download URL
                        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/downloadFile/")
                                .path(fileName)
                                .toUriString();

                        return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());

                } catch (Exception e) {
                        throw new RuntimeException("Error while saving file", e);
                }
        }
}
