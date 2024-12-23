package ntou.cse.backend.UserInformation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String email;
    private String role;
    private boolean isBanned;
    private LocalDateTime unbanTime;

    public User() {}

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean getIsBanned() {return isBanned;}

    public LocalDateTime getUnbanTime() {return unbanTime;}

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setIsBanned(boolean isBanned) {this.isBanned = isBanned;}

    public void setUnbanTime(LocalDateTime unbanTime) {this.unbanTime = unbanTime;}

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", isBanned=" + isBanned + '\'' +
                ", unbanTime=" + unbanTime +
                '}';
    }

}
