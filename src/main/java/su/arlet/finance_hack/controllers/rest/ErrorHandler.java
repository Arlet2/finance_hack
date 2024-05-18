package su.arlet.finance_hack.controllers.rest;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import su.arlet.finance_hack.exceptions.EntityNotFoundException;
import su.arlet.finance_hack.exceptions.UserAlreadyExistsException;
import su.arlet.finance_hack.exceptions.UserNotFoundException;
import su.arlet.finance_hack.exceptions.WasteAlreadyDeletedException;

@RestControllerAdvice
class ErrorHandler {

    private final Counter serverErrorCounter;
    private final Counter badRequestErrorCounter;
    private final Counter conflictErrorCounter;
    private final Counter unauthorizedErrorCounter;
    private final Counter contentErrorCounter;

    @Autowired
    public ErrorHandler(MeterRegistry meterRegistry) {
        serverErrorCounter = meterRegistry.counter("server_error_counter");
        badRequestErrorCounter = meterRegistry.counter("bad_request_error_counter");
        conflictErrorCounter = meterRegistry.counter("conflict_error_counter");
        unauthorizedErrorCounter = meterRegistry.counter("unauthorized_error_counter");
        contentErrorCounter = meterRegistry.counter("content_error_counter");

    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleAllException(Exception e, HttpServletRequest request) {
        serverErrorCounter.increment();
        System.out.println("Error in " + request.getMethod() + " " + request.getRequestURL() + ": " + e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(ValidationException e) {
        badRequestErrorCounter.increment();
        return "Bad body: " + e.getMessage();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFoundException(EntityNotFoundException e) {
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        conflictErrorCounter.increment();
        return "this username has been already taken";
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUserNotFoundException(UserNotFoundException e) {
        unauthorizedErrorCounter.increment();
        return "login is incorrectly set / not set at all";
    }

    @ExceptionHandler(WasteAlreadyDeletedException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String handleWasteAlreadyDeletedException(WasteAlreadyDeletedException e) {
        contentErrorCounter.increment();
        return "object has already been deleted";
    }


}
