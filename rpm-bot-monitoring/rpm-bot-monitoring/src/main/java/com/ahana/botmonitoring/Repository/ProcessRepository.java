package com.ahana.botmonitoring.Repository;

import com.ahana.botmonitoring.Entity.ProcessModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessRepository extends MongoRepository<ProcessModel, String> {
    Optional<ProcessModel> findByProcessId(Integer processId);

    List<ProcessModel> findByBotId(Integer botId);

    Optional<ProcessModel> findByProcessName(String processName);

    @Deprecated
    List<ProcessModel> findByBotName(String botName);

    Optional<ProcessModel> findTopByOrderByProcessIdDesc();
}
