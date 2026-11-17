package ao.vanadio.publicinformationapi.bootstrap.web;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
