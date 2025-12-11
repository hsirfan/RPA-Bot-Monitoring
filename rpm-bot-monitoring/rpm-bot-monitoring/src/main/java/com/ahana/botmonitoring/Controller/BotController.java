package com.ahana.botmonitoring.Controller;

import com.ahana.botmonitoring.Service.BotService;
import com.ahana.botmonitoring.generated.api.BotControllerApi;
import com.ahana.botmonitoring.generated.model.AddBotResponse;
import com.ahana.botmonitoring.generated.model.UpdateBotResponse;
import com.ahana.botmonitoring.generated.model.BotDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin(origins = "${app.cors.allowed-origin}")
public class BotController implements BotControllerApi {

    private final BotService botService;

    public BotController(BotService botService) {
        this.botService = botService;
    }

    @Override
    public ResponseEntity<AddBotResponse> saveBot(BotDTO botDTO) {
        return ResponseEntity.ok(botService.saveBot(botDTO));
}

    @Override
    public ResponseEntity<UpdateBotResponse> updateBot(BotDTO botDTO) {
        return ResponseEntity.ok(botService.updateBot(botDTO));
    }

    @Override
    public ResponseEntity<List<BotDTO>> getAllBot() {
        return ResponseEntity.ok(botService.getAllBot());
    }

    @Override
    public ResponseEntity<BotDTO> getBot(Integer botId) {
            Optional<BotDTO> bot = botService.getBot(botId);
            return bot.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<String> deleteBot(Integer botId) {
        log.info("Deleting bot: {}", botId);
            return ResponseEntity.ok(botService.deleteBot(botId));
    }

    @Override
    public ResponseEntity<String> getBotStatus(String name, String status, String processName, String lastrunTimeStamp) {
        return ResponseEntity.ok(botService.updateBotStatus(name, status, processName, lastrunTimeStamp));
    }
}
