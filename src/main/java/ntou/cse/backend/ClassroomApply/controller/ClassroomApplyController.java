package ntou.cse.backend.ClassroomApply.controller;

import ntou.cse.backend.ClassroomApply.exception.UserBannedException;
import ntou.cse.backend.ClassroomApply.model.ClassroomApply;
import ntou.cse.backend.ClassroomApply.service.ClassroomApplyService;
import ntou.cse.backend.ClassroomBuild.service.ClassroomService;
import ntou.cse.backend.UserInformation.model.User;
import ntou.cse.backend.UserInformation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classroom_apply")
public class ClassroomApplyController {

    @Autowired
    private ClassroomApplyService classroomApplyService;
    @Autowired
    private UserService userService;
    @Autowired
    private ClassroomService classroomService;

    @PostMapping("/apply")
    public ResponseEntity<String> applyForClassroom(@RequestParam String floor,
                                                    @RequestParam String classroomCode,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                                    @RequestParam String borrower,
                                                    @RequestParam String userEmail) {
        // System.out.println("Received borrower: " + borrower);

        try {
            User targetUser = userService.getUserByEmail(userEmail);
            LocalDateTime unbanTime = classroomService.getUnbanTimeByRoomNumber(classroomCode);
            try {
                classroomApplyService.createApplication(floor, classroomCode, startTime, endTime, borrower, targetUser, unbanTime);
                return new ResponseEntity<>("Application created successfully", HttpStatus.OK);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (UserBannedException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
            } catch (Exception ex) {
                return new ResponseEntity<>("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }

    }

    @GetMapping
    public List<ClassroomApply> getAllApplications() {

        return classroomApplyService.getAllApplications();
    }

    @GetMapping("/pending")
    public List<ClassroomApply> getAllPendingApplications() {

        return classroomApplyService.getAllPendingApplications();
    }

    @GetMapping("/{id}")
    public ClassroomApply getApplicationById(@PathVariable String id) {
        return classroomApplyService.getApplicationById(id);
    }

    @PutMapping("/{id}/approve")
    public ClassroomApply approveApplication(@PathVariable String id) {
        return classroomApplyService.updateApplicationApprovalStatus(id, true);
    }

    @PutMapping("/{id}/deny")
    public ClassroomApply denyApplication(@PathVariable String id, @RequestBody Map<String, String> requestBody) {
        return classroomApplyService.updateApplicationApprovalStatus(id, false);
    }
    @GetMapping("/search")
    public List<ClassroomApply> searchApplicationsByClassroomAndTime(
            @RequestParam String floor,
            @RequestParam String roomNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        return classroomApplyService.findApplicationsByClassroomAndTime(floor, roomNumber, startTime, endTime);
    }
    @GetMapping("/borrower/{borrower}")
    public List<ClassroomApply> getApplicationsByBorrower(@PathVariable String borrower) {
        return classroomApplyService.findApplicationsByBorrower(borrower);
    }

}
