package ntou.cse.backend.ClassroomBuild.service;

import ntou.cse.backend.ClassroomBuild.model.Classroom;
import ntou.cse.backend.ClassroomBuild.repo.ClassroomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClassroomService {

    @Autowired
    private ClassroomRepository classroomRepository;

    public List<Classroom> getAllClassrooms() {
        return classroomRepository.findAll();
    }

    public List<String> getAllFloors() {
        Set<String> floors = classroomRepository.findAll().stream()
                .map(Classroom::getFloor)
                .collect(Collectors.toSet());

        // 自定義排序邏輯，將英文樓層放在數字樓層前
        return floors.stream()
                .sorted((f1, f2) -> {
                    boolean isF1Numeric = f1.matches("\\d+");
                    boolean isF2Numeric = f2.matches("\\d+");

                    // 如果 f1 是數字而 f2 是英文，f2 應排在前面
                    if (isF1Numeric && !isF2Numeric) {
                        return 1;
                    }
                    // 如果 f2 是數字而 f1 是英文，f1 應排在前面
                    if (!isF1Numeric && isF2Numeric) {
                        return -1;
                    }
                    // 都是數字或都是英文，按自然順序排序
                    return f1.compareTo(f2);
                })
                .collect(Collectors.toList());
    }


    public List<Classroom> getClassroomsByFloor(String floor) {
        return classroomRepository.findByFloor(floor);
    }

    public Classroom getClassroomByRoomNumber(String roomNumber) {
        return classroomRepository.findByRoomNumber(roomNumber).orElse(null);
    }

    public List<Classroom> searchClassroomsByKeyword(String keyword) {
        return classroomRepository.findByFloorContainingOrRoomNumberContaining(keyword, keyword);
    }

    public Classroom updateKeyStatusAndBorrower(String id, Classroom.KeyStatus keyStatus, String borrower, String borrowerRole) {
        Optional<Classroom> applicationOptional = classroomRepository.findById(id);
        if (applicationOptional.isPresent()) {
            Classroom application = applicationOptional.get();
            application.setKeyStatus(keyStatus);
            application.setBorrower(borrower);
            application.setBorrowerRole(borrowerRole);
            return classroomRepository.save(application);
        }
        return null;
    }

    public Classroom banClassroomByRoomNumber(String roomNumber, Integer unbanTime) {
        Optional<Classroom> classroomOptional = classroomRepository.findByRoomNumber(roomNumber);
        if (classroomOptional.isPresent()) {
            Classroom classroom = classroomOptional.get();
            classroom.setIsBanned(true);
            classroom.setUnbanTime(LocalDateTime.now().plusSeconds(unbanTime));
            return classroomRepository.save(classroom);
        }
        return null;
    }

    public Classroom unbanClassroomByRoomNumber(String roomNumber) {
        Optional<Classroom> classroomOptional = classroomRepository.findByRoomNumber(roomNumber);
        if (classroomOptional.isPresent()) {
            Classroom classroom = classroomOptional.get();
            classroom.setIsBanned(false);
            classroom.setUnbanTime(null);
            return classroomRepository.save(classroom);
        }
        return null;
    }

    public List<Classroom> unbanAllClassrooms() {
        List<Classroom> classrooms = classroomRepository.findAll();
        for (Classroom classroom : classrooms) {
            classroom.setIsBanned(false);
            classroom.setUnbanTime(null);
        }
        return classroomRepository.saveAll(classrooms);
    }

    @Scheduled(fixedRate = 1000)
    public void checkUnbanClassrooms() {
        LocalDateTime now = LocalDateTime.now();
        List<Classroom> bannedClassrooms = classroomRepository.findByIsBannedTrue();
        for (Classroom classroom : bannedClassrooms) {
            if (classroom.getUnbanTime() != null && classroom.getUnbanTime().isBefore(now)) {
                unbanClassroomByRoomNumber(classroom.getRoomNumber());
            }
        }
    }

    public LocalDateTime getUnbanTimeByRoomNumber(String roomNumber) {
        Optional<Classroom> classroomOptional = classroomRepository.findByRoomNumber(roomNumber);
        if (classroomOptional.isPresent()) {
            return classroomOptional.get().getUnbanTime();
        }
        return null; // 若找不到對應教室，回傳 null
    }
}
