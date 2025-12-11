package com.ahana.botmonitoring.Mapper;

import com.ahana.botmonitoring.Entity.ExecutionTime;
import com.ahana.botmonitoring.generated.model.ExecutionTimeDTO;
import com.ahana.botmonitoring.Util.DateConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExecutionTimeMapper {

    public ExecutionTimeDTO toDTO(ExecutionTime entity) {
        if (entity == null)
            return null;
        return ExecutionTimeDTO.builder()
                .logDate(entity.getLogDate())
                .startTime(entity.getStartTime() != null ? LocalDate.parse(entity.getStartTime()) : null)
                .endTime(entity.getEndTime() != null ? LocalDate.parse(entity.getEndTime()) : null)
                .processName(entity.getProcessName())
                .botName(entity.getBotName())
                .timeDifference(entity.getTimeDifference())
                .logDateinLocalDate(entity.getLogDateinLocalDate())
                .startTimeinLocalDateTime(entity.getStartTimeinLocalDateTime())
                .processStatus(entity.getProcessStatus())
                .endTimeinLocalDateTime(entity.getEndTimeinLocalDateTime())
                .errorMessage(entity.getErrorMessage())
                .machineName(entity.getMachineName())
                .createdDate(DateConverter.toLocalDateTime(entity.getCreatedDate()))
                .jobId(entity.getJobId())
                .build();
    }

    public ExecutionTime toEntity(ExecutionTimeDTO dto) {
        if (dto == null)
            return null;
        return ExecutionTime.builder()
                .logDate(dto.getLogDate())
                .startTime(dto.getStartTime() != null ? dto.getStartTime().toString() : null)
                .endTime(dto.getEndTime() != null ? dto.getEndTime().toString() : null)
                .processName(dto.getProcessName())
                .botName(dto.getBotName())
                .timeDifference(dto.getTimeDifference())
                .logDateinLocalDate(dto.getLogDateinLocalDate())
                .startTimeinLocalDateTime(dto.getStartTimeinLocalDateTime())
                .processStatus(dto.getProcessStatus())
                .endTimeinLocalDateTime(dto.getEndTimeinLocalDateTime())
                .errorMessage(dto.getErrorMessage())
                .machineName(dto.getMachineName())
                .createdDate(DateConverter.toDate(dto.getCreatedDate()))
                .jobId(dto.getJobId())
                .build();
    }

    public List<ExecutionTimeDTO> toDTOList(List<ExecutionTime> entities) {
        if (entities == null)
            return null;
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ExecutionTime> toEntityList(List<ExecutionTimeDTO> dtos) {
        if (dtos == null)
            return null;
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
