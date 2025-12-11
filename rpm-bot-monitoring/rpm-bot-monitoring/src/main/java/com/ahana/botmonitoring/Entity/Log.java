package com.ahana.botmonitoring.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "Log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Log {
    @Transient
    public static final String SEQUENCE_NAME = "users_sequence";

    private String message;
    private String level;
    private String logType;
    private LocalDateTime timeStamp;

    @Id
    private String fingerprint;
    private String windowsIdentity;
    private String machineName;
    private String processName;
    private String processVersion;
    private String jobId;
    private String robotName;
    private String machineId;
    private String fileName;
    private String initiatedBy;
    private String totalExecutionTimeInSeconds;
    private String totalExecutionTime;
    private String logStatus;
    private Integer botId;
    private String botName;
}
