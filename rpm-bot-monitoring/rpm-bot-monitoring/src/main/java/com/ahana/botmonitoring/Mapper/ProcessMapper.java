package com.ahana.botmonitoring.Mapper;

import com.ahana.botmonitoring.Entity.ProcessModel;
import com.ahana.botmonitoring.generated.model.ProcessDTO;
import com.ahana.botmonitoring.Util.DateConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcessMapper {

    public ProcessDTO toDTO(ProcessModel entity) {
        if (entity == null) return null;
        return ProcessDTO.builder()
                .processId(entity.getProcessId() != null ? entity.getProcessId().longValue() : null)
                .processName(entity.getProcessName())
                .botName(entity.getBotName())
                .productionMovedDate(entity.getProductionMovedDate())
                .processRequirements(entity.getProcessRequirements())
                .processOwnerName(entity.getProcessOwnerName())
                .processOwnerContact(entity.getProcessOwnerContact())
                .processOwnerMailID(entity.getProcessOwnerMailID())
                .processTeamMember(entity.getProcessTeamMember())
                .lastRunTimeStamp(entity.getLastRunTimeStamp())
                .processLastRunTime(entity.getProcessLastRunTime())
                .processStatus(entity.getProcessStatus())
                .build();
    }

    public ProcessModel toEntity(ProcessDTO dto) {
        if (dto == null) return null;
        
        // Validate that processId fits in Integer range if provided
        Integer processId = null;
        if (dto.getProcessId() != null) {
            if (dto.getProcessId() > Integer.MAX_VALUE || dto.getProcessId() < Integer.MIN_VALUE) {
                throw new IllegalArgumentException("Process ID " + dto.getProcessId() + " is too large to fit in Integer range. Maximum value is " + Integer.MAX_VALUE);
            }
            processId = dto.getProcessId().intValue();
        }
        
        return ProcessModel.builder()
                .processId(processId)
                .processName(dto.getProcessName())
                .botName(dto.getBotName())
                .productionMovedDate(dto.getProductionMovedDate())
                .processRequirements(dto.getProcessRequirements())
                .processOwnerName(dto.getProcessOwnerName())
                .processOwnerContact(dto.getProcessOwnerContact())
                .processOwnerMailID(dto.getProcessOwnerMailID())
                .processTeamMember(dto.getProcessTeamMember())
                .lastRunTimeStamp(dto.getLastRunTimeStamp())
                .processLastRunTime(dto.getProcessLastRunTime())
                .processStatus(dto.getProcessStatus())
                .build();
    }

    public List<ProcessDTO> toDTOList(List<ProcessModel> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProcessModel> toEntityList(List<ProcessDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}

