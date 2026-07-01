package id.my.tudemaha.lobos.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("user not found, invalid user id");
    }
}
