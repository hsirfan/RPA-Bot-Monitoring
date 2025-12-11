package com.ahana.botmonitoring.Repository;

import com.ahana.botmonitoring.Entity.LogPython;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogPythonRepository extends MongoRepository<LogPython,String> {
}
