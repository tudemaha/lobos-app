package id.my.tudemaha.lobos.exception;


public class LoginException extends  RuntimeException{
    public LoginException() {
        super("invalid email or password");
    }
}
