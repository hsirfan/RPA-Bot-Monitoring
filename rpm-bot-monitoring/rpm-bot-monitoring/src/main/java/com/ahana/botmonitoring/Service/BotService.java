package com.ahana.botmonitoring.Service;

import com.ahana.botmonitoring.Exception.CustomException;
import com.ahana.botmonitoring.Entity.BotModel;
import com.ahana.botmonitoring.Entity.ProcessModel;
import com.ahana.botmonitoring.Mapper.BotMapper;
import com.ahana.botmonitoring.Repository.BotRepository;
import com.ahana.botmonitoring.generated.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BotService {
    // Service for Bot operations

    private final BotRepository botRepository;
    private final BotMapper botMapper;
    private final MongoTemplate mongoTemplateRef;

    public BotService(BotRepository botRepository, BotMapper botMapper, MongoTemplate mongoTemplateRef) {
        this.botRepository = botRepository;
        this.botMapper = botMapper;
        this.mongoTemplateRef = mongoTemplateRef;
    }
    public static final String UPDATE_STATUS_MESSAGE =
            "Successfully updated %s status as %s, last run/current running process %s updated with status %s";

    public AddBotResponse saveBot(BotDTO botDTO) {
        BotModel bot = botMapper.toEntity(botDTO);
        if (bot.getBotId() == null) {
            Integer maxBotId = botRepository.findTopByOrderByBotIdDesc()
                    .map(BotModel::getBotId)
                    .orElse(0);
            bot.setBotId(maxBotId + 1);
        }
        botRepository.save(bot);

        AddBotResponse response = new AddBotResponse();
        response.setStatus("Successfully Added Bot");
        response.setId(bot.getBotId());
        response.setBotName(bot.getBotName());

        return response;
    }

    public UpdateBotResponse updateBot(BotDTO botDTO) {
        BotModel botModel = botMapper.toEntity(botDTO);

        if (botModel.getBotId() == null) {
            throw new CustomException("Bot ID is required for update.");
        }

        BotModel existingBot = botRepository.findByBotId(botModel.getBotId())
                .orElseThrow(() -> new CustomException("Bot with ID " + botModel.getBotId() + " not found."));

        existingBot.setBotName(botModel.getBotName());
        existingBot.setIp(botModel.getIp());
        existingBot.setHostName(botModel.getHostName());
        existingBot.setLicenceType(botModel.getLicenceType());
        existingBot.setStartDate(botModel.getStartDate());
        existingBot.setEndDate(botModel.getEndDate());
        existingBot.setUiPathVersion(botModel.getUiPathVersion());
        existingBot.setSoftwareInstalled(botModel.getSoftwareInstalled());
        existingBot.setThresHoldTime(botModel.getThresHoldTime());
        existingBot.setLiveBotStatus(botModel.getLiveBotStatus());
        botRepository.save(existingBot);

        UpdateBotResponse response = new UpdateBotResponse();
        response.setStatus("Bot updated successfully");
        response.setId(existingBot.getBotId());

        return response;
    }

    public List<BotDTO> getAllBot() {
        return botMapper.toDTOList(botRepository.findAll());
    }

    public String deleteBot(Integer botId) {
        BotModel bot = botRepository.findByBotId(botId)
                .orElseThrow(() -> {
                    log.warn("Bot not found: {}", botId);
                    return new CustomException("Bot with ID " + botId + " not found.");
                });
        botRepository.deleteById(bot.getId());
        return "Bot Deleted Successfully";
    }

    public Optional<BotDTO> getBot(Integer botId) {
        return botRepository.findByBotId(botId)
                .map(botMapper::toDTO);
    }

    public Optional<BotDTO> getBotByName(String botName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("botName").is(botName));
        List<BotModel> bots = mongoTemplateRef.find(query, BotModel.class);
        return bots != null && !bots.isEmpty() ? Optional.of(botMapper.toDTO(bots.get(0))) : Optional.empty();
    }

    public StatusUpdateResponse updateBotStatus(String name, String status, String processName, String lastrunTimeStamp) {

        Query botQuery = new Query();
        botQuery.addCriteria(Criteria.where("ip").is(name));

        Update botUpdate = new Update();
        botUpdate.set("liveBotStatus", status);
        botUpdate.set("processName", processName);

        if (status.contains("running")) {
            botUpdate.set("lastRunTimeStamp", lastrunTimeStamp);
        }

        mongoTemplateRef.findAndModify(botQuery, botUpdate, BotModel.class);

        Query processQuery = new Query();
        processQuery.addCriteria(Criteria.where("processName").is(processName));

        Update processUpdate = new Update();
        processUpdate.set("processStatus", status);

        if (status.contains("running")) {
            processUpdate.set("lastRunTimeStamp", lastrunTimeStamp);
            processUpdate.set("processLastRunTime", lastrunTimeStamp);
        }

        mongoTemplateRef.findAndModify(processQuery, processUpdate, ProcessModel.class);

        StatusUpdateResponse response = new StatusUpdateResponse();
        response.setName(name);
        response.setStatus(StatusUpdateResponse.StatusEnum.valueOf(status));
        response.setProcessName(processName);
        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage(
                String.format(UPDATE_STATUS_MESSAGE, name, status, processName, status
                )
        );

       return response;
    }

}
