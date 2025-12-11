package com.ahana.botmonitoring.Service;

import com.ahana.botmonitoring.Exception.CustomException;
import com.ahana.botmonitoring.Entity.UserModel;
import com.ahana.botmonitoring.Mapper.UserMapper;
import com.ahana.botmonitoring.Repository.UserRepository;
import com.ahana.botmonitoring.generated.model.UserDTO;
import com.ahana.botmonitoring.generated.model.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    public UserService(UserRepository userRepository, UserMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public UserResponse adduser(UserDTO userDTO) {
        UserModel user = mapper.toEntity(userDTO);

        if (user.getUserid() == null) {
            Integer maxUserId = userRepository.findTopByOrderByUseridDesc()
                    .map(UserModel::getUserid)
                    .orElse(0);
            user.setUserid(maxUserId + 1);
        }

        userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setStatus("Successfully Added User");
        response.setId(user.getUserid());

        return response;
    }

    public List<UserDTO> getAllUser() {
        return mapper.toDTOList(userRepository.findAll());
    }

    public UserResponse updateUser(UserDTO userDTO) {
        UserModel userModel = mapper.toEntity(userDTO);

        // User ID is required for update
        if (userModel.getUserid() == null) {
            throw new CustomException("User ID is required for update.");
        }

        UserModel existingUser = userRepository.findByUserid(userModel.getUserid())
                .orElseThrow(() -> new CustomException(
                        "User with ID " + userModel.getUserid() + " not found."));

        existingUser.setEmailID(userModel.getEmailID());
        existingUser.setUserName(userModel.getUserName());
        existingUser.setDomainName(userModel.getDomainName());
        existingUser.setPassword(userModel.getPassword());
        existingUser.setMobileNo(userModel.getMobileNo());
        existingUser.setProcess(userModel.getProcess());
        existingUser.setBots(userModel.getBots());
        existingUser.setRole(userModel.getRole());

        userRepository.save(existingUser);

        UserResponse response = new UserResponse();
        response.setStatus("User updated successfully");
        response.setId(existingUser.getUserid());

        return response;

    }

}
