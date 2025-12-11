package com.ahana.botmonitoring.Repository;

import com.ahana.botmonitoring.Entity.UserModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserModel, String> {
    Optional<UserModel> findByUserid(Integer userid);

    UserModel findByEmailID(String emailID);

    Optional<UserModel> findTopByOrderByUseridDesc();
}
