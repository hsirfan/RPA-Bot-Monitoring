package com.ahana.botmonitoring.Mapper;

import com.ahana.botmonitoring.Entity.BotModel;
import com.ahana.botmonitoring.generated.model.BotDTO;
import com.ahana.botmonitoring.Util.DateConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BotMapper {

    public BotDTO toDTO(BotModel entity) {
        if (entity == null) return null;
        return BotDTO.builder()
                .botId(entity.getBotId() != null ? entity.getBotId().longValue() : null)
                .botName(entity.getBotName())
                .ip(entity.getIp())
                .hostName(entity.getHostName())
                .licenceType(entity.getLicenceType())
                .startDate(DateConverter.toLocalDateTime(entity.getStartDate()))
                .endDate(DateConverter.toLocalDateTime(entity.getEndDate()))
                .uiPathVersion(entity.getUiPathVersion())
                .softwareInstalled(entity.getSoftwareInstalled())
                .thresHoldTime(DateConverter.toLocalDateTime(entity.getThresHoldTime()))
                .licenceKey(entity.getLicenceKey())
                .licenceStartDate(DateConverter.toLocalDateTime(entity.getLicenceStartDate()))
                .licenceEndDate(DateConverter.toLocalDateTime(entity.getLicenceEndDate()))
                .random(entity.getRandom())
                .liveBotStatus(entity.getLiveBotStatus())
                .processName(entity.getProcessName())
                .machineName(entity.getMachineName())
                .lastRunLogtimestamp(entity.getLastRunLogtimestamp())
                .lastRunTimeStamp(entity.getLastRunTimeStamp())
                .build();
    }

    public BotModel toEntity(BotDTO dto) {
        if (dto == null) return null;
        return BotModel.builder()
                .botId(dto.getBotId() != null ? dto.getBotId().intValue() : null)
                .botName(dto.getBotName())
                .ip(dto.getIp())
                .hostName(dto.getHostName())
                .licenceType(dto.getLicenceType())
                .startDate(DateConverter.toDate(dto.getStartDate()))
                .endDate(DateConverter.toDate(dto.getEndDate()))
                .uiPathVersion(dto.getUiPathVersion())
                .softwareInstalled(dto.getSoftwareInstalled())
                .thresHoldTime(DateConverter.toDate(dto.getThresHoldTime()))
                .licenceKey(dto.getLicenceKey())
                .licenceStartDate(DateConverter.toDate(dto.getLicenceStartDate()))
                .licenceEndDate(DateConverter.toDate(dto.getLicenceEndDate()))
                .random(dto.getRandom())
                .liveBotStatus(dto.getLiveBotStatus())
                .processName(dto.getProcessName())
                .machineName(dto.getMachineName())
                .lastRunLogtimestamp(dto.getLastRunLogtimestamp())
                .lastRunTimeStamp(dto.getLastRunTimeStamp())
                .build();
    }

    public List<BotDTO> toDTOList(List<BotModel> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BotModel> toEntityList(List<BotDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}

