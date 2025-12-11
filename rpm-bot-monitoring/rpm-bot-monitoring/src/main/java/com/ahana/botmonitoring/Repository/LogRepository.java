package com.ahana.botmonitoring.Repository;

import com.ahana.botmonitoring.Entity.Log;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends MongoRepository<Log, String> {

    List<Log> findByBotNameAndTimeStampBetween(
            String botName,
            LocalDateTime start,
            LocalDateTime end
    );
}
