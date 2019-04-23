package exchangetask.exception;

public class RequestRejectedException extends Exception {
    public RequestRejectedException() {
        super();
    }

    public RequestRejectedException(String message) {
        super(message);
    }
}
