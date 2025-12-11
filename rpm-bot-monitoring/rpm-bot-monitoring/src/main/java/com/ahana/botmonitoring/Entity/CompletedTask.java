package com.ahana.botmonitoring.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "completedTask")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletedTask {
    @Id
    private String id;
    private String jobId;
    private String totalExecutionTime;
}
