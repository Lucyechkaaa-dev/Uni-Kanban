package controllers;

import backend.services.InvalidCredentialsException;
import controllers.ApiDtos.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * One place that turns the exceptions of the services into HTTP answers: {"error": "text"} and a status code.
 * 400 wrong input, 401 not logged in or wrong password, 403 no right, 404 not found.
 * Anything else stays a 500 (Spring's own answer, no stack trace is sent out).
 */
@Log4j2
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> unauthorized(InvalidCredentialsException e) {
        return reply(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> forbidden(SecurityException e) {
        log.warn("Access denied: {}", e.getMessage());
        return reply(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> notFound(NoSuchElementException e) {
        return reply(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException e) {
        return reply(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadableBody(HttpMessageNotReadableException e) {
        return reply(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    private static ResponseEntity<ErrorResponse> reply(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }
}
