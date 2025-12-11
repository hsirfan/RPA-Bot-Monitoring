package com.ahana.botmonitoring.Controller;

import com.ahana.botmonitoring.Entity.Log;
import com.ahana.botmonitoring.Service.LogService;
import com.ahana.botmonitoring.generated.api.LogControllerApi;
import com.ahana.botmonitoring.generated.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "${app.cors.allowed-origin}")
public class LogController implements LogControllerApi {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @Override
    public ResponseEntity<String> processLogFile(MultipartFile file) {
        try {
            return ResponseEntity.ok(logService.processLogFile(file));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing log file: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> processPythonLogFile(MultipartFile file) {
        try {
            return ResponseEntity.ok(logService.processPythonLogFile(file));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing Python log file: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<AllBotDetailUtilisationResponse>> requestAllBotDetailUtilisation(
            RequestAllBotUtilisation requestAllBotUtilisation) {
        return ResponseEntity.ok(logService.getAllBotDetailUtilisation(requestAllBotUtilisation));
    }

    @Override
    public ResponseEntity<Object> requestAllBotUtilisationNew(RequestAllBotUtilisation requestAllBotUtilisation) {
        return ResponseEntity.ok(logService.calculateAllBotUtilisation(requestAllBotUtilisation));
    }

    @Override
    public ResponseEntity<Object> requestBotUtilisationNew(RequestBotUtilisation requestBotUtilisation) {
        return ResponseEntity.ok(logService.getBotUtilisationNew(requestBotUtilisation));
    }

    @Override
    public ResponseEntity<List<ExecutionTimeDTO>> filterLogsNew(RequestUtilisationNew requestUtilisationNew) {
        return ResponseEntity.ok(logService.filterLogsNew(requestUtilisationNew));
    }

    @Override
    public ResponseEntity<List<ExecutionTimeDTO>> filterLogs(RequestUtilsation requestUtilsation) {
        return ResponseEntity.ok(logService.filterLogs(requestUtilsation));
    }

    @Override
    public ResponseEntity<List<String>> getBotNames() {
        return ResponseEntity.ok(logService.getUniqueBotNames());
    }

    @Override
    public ResponseEntity<Object> getPythonUtilisation(RequestPythonUtilisation requestPythonUtilisation) {
        return ResponseEntity.ok(logService.getPythonUtilisation(requestPythonUtilisation));
    }

    @Override
    public ResponseEntity<String> uploadLogFile(String body) {
        return ResponseEntity.ok(logService.processAndSaveLogs(body));
    }

    @Override
    public ResponseEntity<List<ExecutionTimeDTO>> getDetailsLogByDateRange(
            @PathVariable String botName,
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(logService.getDetailsLogByDateRange(botName, startDate, endDate));
    }

}
