package ntou.cse.backend.UserInformation.exception;

public class UserAlreadyBannedLongerException extends RuntimeException {
    public UserAlreadyBannedLongerException(String message) {
        super(message);
    }
}
