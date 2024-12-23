package ntou.cse.backend.UserInformation.repo;

import ntou.cse.backend.UserInformation.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepo extends MongoRepository<User, String> {
    User findByEmail(String email);
    List<User> findByIsBannedTrue();
}
