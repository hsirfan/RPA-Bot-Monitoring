package com.ahana.botmonitoring.Controller;

import com.ahana.botmonitoring.Service.LoginService;
import com.ahana.botmonitoring.generated.api.LoginControllerApi;
import com.ahana.botmonitoring.generated.model.LoginDTO;
import com.ahana.botmonitoring.generated.model.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "${app.cors.allowed-origin}")
public class LoginController implements LoginControllerApi {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public ResponseEntity<LoginResponse> login(LoginDTO loginDTO) {
        Map<String, Object> result = loginService.login(loginDTO);
        LoginResponse response = new LoginResponse();
        response.setSuccess((Boolean) result.get("success"));
        response.setToken((String) result.get("token"));
        response.setUser((com.ahana.botmonitoring.generated.model.UserDTO) result.get("user"));
        return ResponseEntity.ok(response);
    }
}
