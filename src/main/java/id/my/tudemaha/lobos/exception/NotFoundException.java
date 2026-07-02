package id.my.tudemaha.lobos.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("resource not found, invalid id");
    }
}
