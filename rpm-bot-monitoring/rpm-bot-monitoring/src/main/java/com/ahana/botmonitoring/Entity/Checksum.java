package com.ahana.botmonitoring.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "Checksum")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Checksum {
    @Id
    private String id;
    private String checksum;
    private Long botId;
    private String botName;
    private String machineName;
    private String processName;
    private String processVersion;
    private String fileuri;
    private LocalDateTime uploadedAt;
}
