package ntou.cse.backend.ClassroomBuild.repo;

import ntou.cse.backend.ClassroomBuild.model.Classroom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends MongoRepository<Classroom, String> {
    Optional<Classroom> findByFloorAndRoomNumber(String floor, String roomNumber);
    List<Classroom> findByFloor(String floor);
    Optional<Classroom> findByRoomNumber(String roomNumber);
    List<Classroom> findByFloorContainingOrRoomNumberContaining(String floor, String roomNumber);
    List<Classroom> findByIsBannedTrue();
}
