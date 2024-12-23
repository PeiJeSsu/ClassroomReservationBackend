package ntou.cse.backend.UserInformation.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import ntou.cse.backend.UserInformation.exception.UserAlreadyBannedLongerException;
import ntou.cse.backend.UserInformation.model.User;
import ntou.cse.backend.UserInformation.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@EnableAsync
public class UserService {

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public User createUser(String email, String role) {
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        user.setIsBanned(false);
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getBorrowers() {
        return userRepository.findAll().stream()
                .filter(user -> "borrower".equals(user.getRole()))
                .collect(Collectors.toList());
    }

    public boolean banUser(String email, int lastTimeInSeconds) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            if (user.getIsBanned() && user.getUnbanTime().isAfter(LocalDateTime.now().plusSeconds(lastTimeInSeconds))) {
                throw new UserAlreadyBannedLongerException(
                        String.format("使用者已被禁用直到 %s，無法禁用更短的時間，若要執行此操作，請先幫使用者解除禁用",
                                user.getUnbanTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")))
                );
            }

            user.setIsBanned(true);
            LocalDateTime unbanTime = LocalDateTime.now().plusSeconds(lastTimeInSeconds);
            user.setUnbanTime(unbanTime);
            userRepository.save(user);

            String emailText = String.format(
                    "Dear user,\n\nYour account has been banned until %s.\n\n" +
                            "If you believe this is a mistake, please contact support.\n\n" +
                            "Best regards,\nSystem Administrator",
                    unbanTime.toString()
            );

            sendEmailAsync(email, "Account Banned", emailText);
            return true;
        }
        return false;
    }

    public boolean unbanUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setIsBanned(false);
            user.setUnbanTime(null);
            userRepository.save(user);

            String emailText = "Dear user,\n\n" +
                    "Your account has been unbanned. You can now access all features again.\n\n" +
                    "Best regards,\nSystem Administrator";

            sendEmailAsync(email, "Account Unbanned", emailText);
            return true;
        }
        return false;
    }

    public void updateAllUsersUnBanned() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            user.setIsBanned(false);
        }
        userRepository.saveAll(allUsers);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Scheduled(fixedRate = 1000)
    public void checkUnbanUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<User> bannedUsers = userRepository.findByIsBannedTrue();
        for (User user : bannedUsers) {
            if (user.getUnbanTime() != null && user.getUnbanTime().isBefore(now)) {
                unbanUser(user.getEmail());
                System.out.println("User " + user.getEmail() + " has been unbanned.");
            }
        }
    }

    @Async
    public CompletableFuture<Void> sendEmailAsync(String to, String subject, String text) {
        return CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(new InternetAddress(fromEmail));
                helper.setTo(new InternetAddress(to));
                helper.setSubject(subject);
                helper.setText(text, true);

                mailSender.send(message);
            } catch (MessagingException e) {
                System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            }
        });
    }
}
