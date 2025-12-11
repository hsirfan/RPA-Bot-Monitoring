package com.ahana.botmonitoring.Repository;


import com.ahana.botmonitoring.Entity.Checksum;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ChecksumRepository extends MongoRepository<Checksum, String> {

    Optional<Checksum> findByProcessNameAndBotId(String processName, Long botId);
    
    @Deprecated
    Optional<Checksum> findByProcessNameAndBotName(String processName, String botName);

    Optional<Checksum> findByChecksum(String checksum);

    @Query("{ 'machineName': ?0, 'processName': ?1, 'processVersion': ?2, 'checksum': ?3, 'fileuri': { $regex: ?4, $options: 'i' } }")
    Optional<Checksum> findByAllFields(
            String machineName,
            String processName,
            String processVersion,
            String checksum,
            String fileNameRegex
    );
}
