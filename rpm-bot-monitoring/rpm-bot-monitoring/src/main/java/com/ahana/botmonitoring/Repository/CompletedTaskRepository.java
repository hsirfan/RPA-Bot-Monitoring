package com.ahana.botmonitoring.Repository;

import com.ahana.botmonitoring.Entity.CompletedTask;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CompletedTaskRepository extends MongoRepository<CompletedTask, String> {
}
