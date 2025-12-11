package com.ahana.botmonitoring.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Date;

@Document(collection = "Bot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotModel {

    @Id
    private String id;

    @Field(name = "botId")
    private Integer botId;

    @Field(name = "botName")
    private String botName;

    @Field(name = "ip")
    private String ip;

    @Field(name = "hostName")
    private String hostName;

    @Field(name = "licenceType")
    private String licenceType;

    @Field(name="startDate")
    private Date startDate;

    @Field(name="endDate")
    private Date endDate;

    @Field(name = "uiPathVersion")
    private String uiPathVersion;

    @Field(name = "softwareInstalled")
    private String softwareInstalled;

    @Field(name = "thresHoldTime")
    private Date thresHoldTime;

    @Field(name = "licenceKey")
    private String licenceKey;

    @Field(name = "licenceStartDate")
    private Date licenceStartDate;

    @Field(name = "licenceEndDate")
    private Date licenceEndDate;

    @Field(name = "random")
    private String random;

    @Field(name="liveBotStatus")
    private String liveBotStatus;

    @Field(name="processName")
    private String processName;

    @Field(name = "machineName")
    private String machineName;

    @Field(name = "lastRunLogtimestamp")
    private LocalDateTime lastRunLogtimestamp;

    private String lastRunTimeStamp;

}
