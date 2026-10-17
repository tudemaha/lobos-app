package id.my.tudemaha.lobos.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("email not verified");
    }

    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
