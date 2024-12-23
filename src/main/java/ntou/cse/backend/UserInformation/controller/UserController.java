package ntou.cse.backend.UserInformation.controller;

import ntou.cse.backend.UserInformation.model.User;
import ntou.cse.backend.UserInformation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            userService.createUser(user.getEmail(), user.getRole());
            return ResponseEntity.ok("註冊成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("註冊失敗：" + e.getMessage());
        }
    }

    @PostMapping("/role")
    public ResponseEntity<?> getUserRole(@RequestBody User user) {
        try {
            User foundUser = userService.getUserByEmail(user.getEmail());

            if (foundUser == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            return ResponseEntity.ok().body(foundUser.getRole());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving user role: " + e.getMessage());
        }
    }

    @GetMapping("/borrowers")
    public ResponseEntity<?> getBorrowers() {
        try {
            List<User> borrowers = userService.getBorrowers();
            return ResponseEntity.ok(borrowers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving borrowers: " + e.getMessage());
        }
    }

    @PatchMapping("/{email}/ban")
    public ResponseEntity<String> banUser(@PathVariable String email, @RequestBody int lastTimeInSeconds) {
        try {
            boolean banned = userService.banUser(email, lastTimeInSeconds);
            if (banned) {
                return ResponseEntity.ok("User banned successfully.");
            } else {
                return ResponseEntity.status(404).body("User not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error banning user: " + e.getMessage());
        }
    }

    @PatchMapping("/{email}/unban")
    public ResponseEntity<String> unbanUser(@PathVariable String email) {
        try {
            boolean unbanned = userService.unbanUser(email);
            if (unbanned) {
                return ResponseEntity.ok("User unbanned successfully.");
            } else {
                return ResponseEntity.status(404).body("User not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error unbanning user: " + e.getMessage());
        }
    }


    @PatchMapping("/unBanAllUsers")
    public ResponseEntity<String> unBanAllUsers() {  // 用來初始化的
        try {
            userService.updateAllUsersUnBanned();
            return ResponseEntity.ok("All users have been unbanned successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error unbanned all users: " + e.getMessage());
        }
    }

    @GetMapping("/allUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
