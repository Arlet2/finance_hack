package su.arlet.finance_hack.controllers.rest;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
