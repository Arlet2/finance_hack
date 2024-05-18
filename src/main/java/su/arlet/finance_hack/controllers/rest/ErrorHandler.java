package su.arlet.finance_hack.controllers.rest;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import su.arlet.finance_hack.exceptions.*;

@RestControllerAdvice
class ErrorHandler {

    private final Counter serverErrorCounter;
    private final Counter badRequestErrorCounter;
    private final Counter conflictErrorCounter;
    private final Counter unauthorizedErrorCounter;
    private final Counter contentErrorCounter;
    private final Counter wrondPasswordCounter;
    private final Counter repoAlreadyDeletedCounter;

    @Autowired
    public ErrorHandler(MeterRegistry meterRegistry) {
        serverErrorCounter = meterRegistry.counter("finance_server_error_counter");
        badRequestErrorCounter = meterRegistry.counter("finance_bad_request_error_counter");
        conflictErrorCounter = meterRegistry.counter("finance_conflict_error_counter");
        unauthorizedErrorCounter = meterRegistry.counter("finance_unauthorized_error_counter");
        contentErrorCounter = meterRegistry.counter("finance_content_error_counter");
        wrondPasswordCounter = meterRegistry.counter("finance_wrong_password_counter");
        repoAlreadyDeletedCounter = meterRegistry.counter("finance_repo_already_delete_counter");
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
    @ExceptionHandler(WrongPasswordException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleWrongPasswordException(WrongPasswordException e) {
        wrondPasswordCounter.increment();
        return "wrong password";
    }

    @ExceptionHandler(RepoAlreadyDeleteException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String handleRepoAlreadyDeleteException(RepoAlreadyDeleteException e) {
        repoAlreadyDeletedCounter.increment();
        return "repo has already been deleted";
    }


}
