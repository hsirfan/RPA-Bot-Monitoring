package com.ahana.botmonitoring.Repository;

import com.ahana.botmonitoring.Entity.PythonExecutionTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PythonExecutionTimeRepository extends MongoRepository<PythonExecutionTime,String> {
}
