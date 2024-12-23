package ntou.cse.backend.ClassroomApply.service;

import ntou.cse.backend.ClassroomApply.exception.UserBannedException;
import ntou.cse.backend.ClassroomApply.model.ClassroomApply;
import ntou.cse.backend.ClassroomApply.repo.ClassroomApplyRepository;
import ntou.cse.backend.UserInformation.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClassroomApplyService {

    @Autowired
    private ClassroomApplyRepository classroomApplyRepository;




    public void createApplication(String floor, String classroomCode, LocalDateTime startTime, LocalDateTime endTime, String borrower, User targetUser, LocalDateTime unbanTime) {

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time.");
        }

        if (targetUser.getIsBanned()) {
            throw new UserBannedException("User is banned. Should not apply classroom.");
        }

        if (unbanTime != null && unbanTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Classroom is banned until " + unbanTime);
        }

        if (floor.isEmpty() || classroomCode.isEmpty()) {
            throw new IllegalArgumentException("Floor and classroom code must not be empty.");
        }
        List<ClassroomApply> existingApplications = classroomApplyRepository.findByBorrowerAndFloorAndClassroomAndStartTimeBeforeAndEndTimeAfter(
                borrower, floor, classroomCode, endTime, startTime);
        if (!existingApplications.isEmpty()) {
            throw new IllegalStateException("Already booked."); // 回傳"已經借用"的錯誤訊息
        }

        List<ClassroomApply> conflictingApplicationsClassroom = classroomApplyRepository.findByFloorAndClassroomAndIsApprovedTrueAndStartTimeBeforeAndEndTimeAfter(
                floor, classroomCode, endTime, startTime);

        if (!conflictingApplicationsClassroom.isEmpty()) {
            throw new IllegalStateException("The classroom is already booked and approved during the requested time.");
        }
        ClassroomApply application = new ClassroomApply();
        application.setFloor(floor);
        application.setClassroom(classroomCode);
        application.setStartTime(startTime);
        application.setEndTime(endTime);
        application.setBorrower(borrower);
        application.setApproved(null);

        classroomApplyRepository.save(application);
    }

    private List<ClassroomApply> findApplicationsByBorrowerAndTime(String borrower, LocalDateTime startTime, LocalDateTime endTime) {
        return classroomApplyRepository.findByBorrowerAndStartTimeBeforeAndEndTimeAfter(borrower, endTime, startTime);
    }

    public List<ClassroomApply> getAllApplications() {
        return classroomApplyRepository.findAll();
    }

    public List<ClassroomApply> getAllPendingApplications() {
        return classroomApplyRepository.findByIsApprovedNull();
    }

    public ClassroomApply getApplicationById(String id) {
        Optional<ClassroomApply> application = classroomApplyRepository.findById(id);
        return application.orElse(null);
    }

    public ClassroomApply updateApplicationApprovalStatus(String id, Boolean isApproved) {
        Optional<ClassroomApply> applicationOptional = classroomApplyRepository.findById(id);
        if (applicationOptional.isPresent()) {
            ClassroomApply application = applicationOptional.get();
            application.setApproved(isApproved);

            return classroomApplyRepository.save(application);
        }
        return null;
    }

    public List<ClassroomApply> findApplicationsByClassroomAndTime(String floor, String classroom, LocalDateTime startTime, LocalDateTime endTime) {
        return classroomApplyRepository.findByFloorAndClassroomAndStartTimeBetweenAndIsApprovedTrue(floor, classroom, startTime, endTime);
    }
    public List<ClassroomApply> findApplicationsByBorrower(String borrower) {
        return classroomApplyRepository.findByBorrower(borrower);
    }
}
