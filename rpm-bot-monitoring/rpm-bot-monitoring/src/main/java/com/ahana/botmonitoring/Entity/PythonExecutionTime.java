package com.ahana.botmonitoring.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "PythonLogExecutionTime")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PythonExecutionTime {
    @Id
    private String id;
    private LocalDate logDateinLocalDate;
    private Date createdDate;
    private LocalDateTime startTimeinLocalDateTime;
    private LocalDateTime endTimeinLocalDateTime;
    private String timeDifference;
    private String processName;
    private String processStatus;
    private List<String> errorMessages;
}
