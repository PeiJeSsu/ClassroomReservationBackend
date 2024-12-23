package ntou.cse.backend.ClassroomBuild.controller;

import jakarta.annotation.PostConstruct;
import ntou.cse.backend.ClassroomBuild.model.Classroom;
import ntou.cse.backend.ClassroomBuild.service.ClassroomInitService;
import ntou.cse.backend.ClassroomBuild.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classroom_build")
public class ClassroomController {
    @Autowired
    private ClassroomInitService classroomInitService;

    @Autowired
    private ClassroomService classroomService;

    @PostConstruct
    public void initializeClassrooms() {
        classroomInitService.initClassrooms();
    }

    @GetMapping("/all")
    public List<Classroom> getAllClassrooms() {
        return classroomService.getAllClassrooms();
    }

    @GetMapping("/floors")
    public List<String> getAllFloors() {
        return classroomService.getAllFloors();
    }

    @GetMapping("/floor/{floor}")
    public List<Classroom> getClassroomsByFloor(@PathVariable String floor) {
        return classroomService.getClassroomsByFloor(floor);
    }

    @GetMapping("/room/{roomNumber}")
    public Classroom getClassroomByRoomNumber(@PathVariable String roomNumber) {
        return classroomService.getClassroomByRoomNumber(roomNumber);
    }

    @GetMapping("/search")
    public List<Classroom> searchClassroomsByKeyword(@RequestParam String keyword) {
        return classroomService.searchClassroomsByKeyword(keyword);
    }

    @PatchMapping("/{id}/update-status")
    public Classroom updateKeyStatusAndBorrower(
            @PathVariable String id,
            @RequestParam Classroom.KeyStatus keyStatus,
            @RequestParam(required = false) String borrower,
            @RequestParam(required = false) String borrowerRole) {
        Classroom updatedClassroom = classroomService.updateKeyStatusAndBorrower(id, keyStatus, borrower, borrowerRole);
        if (updatedClassroom == null) {
            throw new IllegalArgumentException("Classroom not found with id: " + id);
        }
        return updatedClassroom;
    }

    @PatchMapping("/{roomNumber}/ban")
    public Classroom banClassroom(
            @PathVariable String roomNumber,
            @RequestParam Integer unbanTime) {
        Classroom bannedClassroom = classroomService.banClassroomByRoomNumber(roomNumber, unbanTime);
        if (bannedClassroom == null) {
            throw new IllegalArgumentException("Classroom not found with room number: " + roomNumber);
        }
        return bannedClassroom;
    }

    @PatchMapping("/{roomNumber}/unban")
    public Classroom unbanClassroom(@PathVariable String roomNumber) {
        Classroom unbannedClassroom = classroomService.unbanClassroomByRoomNumber(roomNumber);
        if (unbannedClassroom == null) {
            throw new IllegalArgumentException("Classroom not found with room number: " + roomNumber);
        }
        return unbannedClassroom;
    }

    @PatchMapping("/unban-all")
    public List<Classroom> unbanAllClassrooms() {
        return classroomService.unbanAllClassrooms();
    }
}
