package com.ahana.botmonitoring.Repository;

import com.ahana.botmonitoring.Entity.ExecutionTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionTimeRepository extends MongoRepository<ExecutionTime,String> {
}
