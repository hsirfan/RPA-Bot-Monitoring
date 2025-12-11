package com.ahana.botmonitoring.Controller;

import com.ahana.botmonitoring.Service.ProcessService;
import com.ahana.botmonitoring.generated.api.ProcessControllerApi;
import com.ahana.botmonitoring.generated.model.AddProcessResponse;
import com.ahana.botmonitoring.generated.model.BotRequest;
import com.ahana.botmonitoring.generated.model.ProcessDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin(origins = "${app.cors.allowed-origin}")
public class ProcessController implements ProcessControllerApi {

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @Override
    public ResponseEntity<AddProcessResponse> saveNewProcess(ProcessDTO processDTO) {
        return ResponseEntity.ok(processService.saveNewProcess(processDTO));
    }

    @Override
    public ResponseEntity<AddProcessResponse> updateProcess(ProcessDTO processDTO) {
        return ResponseEntity.ok(processService.updateProcess(processDTO));
    }

    @Override
    public ResponseEntity<List<ProcessDTO>> getAllProcess() {
        return ResponseEntity.ok(processService.getAllProcess());
    }

    @Override
    public ResponseEntity<ProcessDTO> getNewProcess(Integer processId) {
            Optional<ProcessDTO> process = processService.getNewProcess(processId);
        return process.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }

    @Override
    public ResponseEntity<String> deleteProcess(Integer processId) {

            return ResponseEntity.ok(processService.deleteProcess(processId));

    }

    @Override
    public ResponseEntity<List<ProcessDTO>> getProcesses(Integer botId) {

            return ResponseEntity.ok(processService.getProcessesByBotId(botId));

    }

    @Override
    public ResponseEntity<List<ProcessDTO>> getProcessDetails(Integer processId) {

            return ResponseEntity.ok(processService.getProcessDetails(processId));

    }

    @Override
    public ResponseEntity<List<String>> getProcessForRegisteredBots(BotRequest botRequest) {
        List<String> processNames = botRequest.getBotName().stream()
                .flatMap(botName -> processService.getProcessForBotByName(botName).stream())
                .map(ProcessDTO::getProcessName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(processNames);
    }
}
