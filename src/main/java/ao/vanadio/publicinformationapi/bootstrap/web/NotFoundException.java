package ao.vanadio.publicinformationapi.bootstrap.web;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
