package com.ahana.botmonitoring.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Document(collection = "ExecutionTime")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionTime {

    @Id
    private String jobId;

    private String logDate;
    private String startTime;
    private String endTime;
    private String processName;
    private Integer botId;
    private String botName;
    private String timeDifference;
    private LocalDate logDateinLocalDate;
    private LocalDateTime startTimeinLocalDateTime;
    private String processStatus;
    private LocalDateTime endTimeinLocalDateTime;
    private List<String> errorMessage;
    private String machineName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private Date createdDate = new Date();


}
