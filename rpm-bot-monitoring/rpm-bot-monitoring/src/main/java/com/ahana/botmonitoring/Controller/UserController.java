package com.ahana.botmonitoring.Controller;

import com.ahana.botmonitoring.Service.UserService;
import com.ahana.botmonitoring.generated.api.UserControllerApi;
import com.ahana.botmonitoring.generated.model.AddProcessResponse;
import com.ahana.botmonitoring.generated.model.UserDTO;
import com.ahana.botmonitoring.generated.model.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "${app.cors.allowed-origin}")
public class UserController implements UserControllerApi {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<UserResponse> adduser(UserDTO userDTO) {
        return ResponseEntity.ok(userService.adduser(userDTO));
    }

    @Override
    public ResponseEntity<List<UserDTO>> getAllUser() {
        return ResponseEntity.ok(userService.getAllUser());
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(userDTO));
    }
}
