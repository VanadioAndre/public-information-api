package ao.vanadio.publicinformationapi.bootstrap.web;

public record PageQuery(int page, int size) {

    public static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 0) throw new BadRequestException("page must be greater than or equal to 0");
        if (size < 1 || size > MAX_SIZE) throw new BadRequestException("size must be between 1 and " + MAX_SIZE);
    }

    public long offset() {
        return (long) page * size;
    }
}
