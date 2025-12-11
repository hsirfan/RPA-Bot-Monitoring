package com.ahana.botmonitoring.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "PythonLog")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogPython {
    @Id
    private String id;
    private LocalDateTime timestamp;
    private String level;
    private String message;
    private String processName;
    private Map<String, Object> additionalFields;
}
