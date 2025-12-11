package com.ahana.botmonitoring.Entity;

import com.ahana.botmonitoring.generated.model.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Document(collection = "ProcessList")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessModel {

    @Id
    private String id;

    @Field(name="processId")
    private Integer processId;

    @Field(name="processName")
    private String processName;

    @Field(name = "botId")
    private List<Integer> botId;
    
    @Field(name = "botName")
    private List<String> botName;

    @Field(name = "productionMovedDate")
    private LocalDate productionMovedDate;

    @Field(name = "processRequirements")
    private String processRequirements;

    @Field(name = "processOwnerName")
    private String processOwnerName;

    @Field(name = "processOwnerContact")
    private String processOwnerContact;

    @Field(name = "processOwnerMailID")
    private String processOwnerMailID;

    @Field(name = "processTeamMember")
    private List<TeamMember> processTeamMember;

    @Field(name = "lastRunTimeStamp")
    private String lastRunTimeStamp;

    @Field(name = "processLastRunTime")
    private String processLastRunTime;

    @Field(name="processStatus")
    private String processStatus;
}
