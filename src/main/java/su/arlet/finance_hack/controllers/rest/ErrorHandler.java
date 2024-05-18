package su.arlet.finance_hack.controllers.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import su.arlet.finance_hack.exceptions.IncorrectUsernameException;
import su.arlet.finance_hack.exceptions.UserNotFoundException;
import su.arlet.finance_hack.exceptions.WasteAlreadyDeletedException;
import su.arlet.finance_hack.exceptions.UserAlreadyExistsException;

@RestControllerAdvice
class ErrorHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleAllException(Exception e, HttpServletRequest request) {
        System.out.println("Error in " + request.getMethod() + " " + request.getRequestURL() + ": " + e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(ValidationException e) {
        return "Bad body: " + e.getMessage();
    }

    //    @ExceptionHandler(EntityNotFoundException::class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    fun handleEntityNotFoundException(e: EntityNotFoundException): String {
//        return "Not found"
//    }
//
//    @ExceptionHandler(UserNotFoundException::class)
//    @ResponseStatus(HttpStatus.CONFLICT)
//    fun handleUserNotFoundException(e: UserNotFoundException): String {
//        return "user not found"
//    }
//
//    @ExceptionHandler(UnsupportedStatusChangeException::class)
//    @ResponseStatus(HttpStatus.CONFLICT)
//    fun handleUnsupportedStatusChangeException(e: UnsupportedStatusChangeException): String {
//        return "unsupported status change"
//    }
//
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return "this username has been already taken";
    }

//
//    @ExceptionHandler(UnauthorizedError::class)
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    fun handleUnauthorizedError(e: UnauthorizedError): String {
//        return "unauthorized"
//    }
//
//    @ExceptionHandler(PermissionDeniedException::class)
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    fun handlePermissionDeniedException(e: PermissionDeniedException): String {
//        return "you don't have access to this ${e.message}"
//    }
//
//    @ExceptionHandler(AccessDeniedException::class)
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    fun handleAccessDeniedException(e: PermissionDeniedException): String {
//        return "you don't have access to this ${e.message}"
//    }
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUserNotFoundException(UserNotFoundException e) {
        return "login is incorrectly set / not set at all";
    }

    @ExceptionHandler(WasteAlreadyDeletedException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public String handleWasteAlreadyDeletedException(WasteAlreadyDeletedException e) {
        return "object has already been deleted";
    }



}
