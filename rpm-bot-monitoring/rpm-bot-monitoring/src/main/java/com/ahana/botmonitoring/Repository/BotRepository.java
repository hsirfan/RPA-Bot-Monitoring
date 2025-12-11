package com.ahana.botmonitoring.Repository;

import com.ahana.botmonitoring.Entity.BotModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BotRepository extends MongoRepository<BotModel, String> {
    Optional<BotModel> findByBotId(Integer botId);

    Optional<BotModel> findTopByOrderByBotIdDesc();
}
