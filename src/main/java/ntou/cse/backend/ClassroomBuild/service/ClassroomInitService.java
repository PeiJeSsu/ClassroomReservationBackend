package ntou.cse.backend.ClassroomBuild.service;

import ntou.cse.backend.ClassroomBuild.model.Classroom;
import ntou.cse.backend.ClassroomBuild.repo.ClassroomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClassroomInitService {

    @Autowired
    private ClassroomRepository classroomRepository;

    private final String[][] classrooms = {
            {"B1", "B07"}, {"B1", "B14"}, {"B1", "B16"},
            {"1", "101"}, {"1", "105"}, {"1", "110"}, {"1", "112"}, {"1", "114"},
            {"2", "201"}, {"2", "203"}, {"2", "205"}, {"2", "212"},
            {"3", "301"}, {"3", "303"}, {"3", "305"},
            {"4", "407"}, {"4", "409"}
    };

    public void initClassrooms() {
        saveClassroom();
        removeExtraClassrooms();
    }

    private void saveClassroom() {
        for (String[] classroomData : classrooms) {
            String floor = classroomData[0];
            String roomNumber = classroomData[1];

            if (!classroomExists(floor, roomNumber)) {
                Classroom classroom = new Classroom();
                classroom.setFloor(floor);
                classroom.setRoomNumber(roomNumber);
                // classroom.setKeyStatus(AVAILABLE);
                // classroom.setBorrower(null);
                // classroom.setBorrowerRole(null);
                classroomRepository.save(classroom);
            }
        }
    }

    private boolean classroomExists(String floor, String roomNumber) {
        Optional<Classroom> existingClassroom = classroomRepository.findByFloorAndRoomNumber(floor, roomNumber);
        return existingClassroom.isPresent();
    }

    private void removeExtraClassrooms() {
        List<Classroom> allClassrooms = classroomRepository.findAll();

        Set<String> predefinedClassroomKeys = List.of(classrooms).stream()
                .map(classroom -> classroom[0] + classroom[1])
                .collect(Collectors.toSet());

        for (Classroom classroom : allClassrooms) {
            String classroomKey = classroom.getFloor() + classroom.getRoomNumber();
            if (!predefinedClassroomKeys.contains(classroomKey)) {
                classroomRepository.delete(classroom);
            }
        }
    }
}
