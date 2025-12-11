package com.ahana.botmonitoring.Mapper;

import com.ahana.botmonitoring.Entity.UserModel;
import com.ahana.botmonitoring.generated.model.UserDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDTO toDTO(UserModel entity) {
        if (entity == null) return null;
        return UserDTO.builder()
                .id(entity.getUserid() != null ? entity.getUserid().longValue() : null)
                .employeeID(entity.getEmployeeID())
                .userName(entity.getUserName())
                .domainName(entity.getDomainName())
                .emailID(entity.getEmailID())
                .mobileNo(entity.getMobileNo())
                .password(entity.getPassword())
                .process(entity.getProcess())
                .bots(entity.getBots())
                .role(entity.getRole())
                .build();
    }

    public UserModel toEntity(UserDTO dto) {
        if (dto == null) return null;
        return UserModel.builder()
                .userid(dto.getId() != null ? dto.getId().intValue() : null)
                .employeeID(dto.getEmployeeID())
                .userName(dto.getUserName())
                .domainName(dto.getDomainName())
                .emailID(dto.getEmailID())
                .mobileNo(dto.getMobileNo())
                .password(dto.getPassword())
                .process(dto.getProcess())
                .bots(dto.getBots())
                .role(dto.getRole())
                .build();
    }

    public List<UserDTO> toDTOList(List<UserModel> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserModel> toEntityList(List<UserDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}

