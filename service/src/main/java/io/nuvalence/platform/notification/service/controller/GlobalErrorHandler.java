package io.nuvalence.platform.notification.service.controller;

import io.nuvalence.platform.notification.service.exception.BadDataException;
import io.nuvalence.platform.notification.service.exception.NotFoundException;
import io.nuvalence.platform.notification.service.exception.ProblemDetail;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Handles exceptions thrown by controllers.
 */
@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    /**
     * Handles ConstraintViolationException.
     *
     * @param exception ConstraintViolationException
     * @return ResponseEntity
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(
            ConstraintViolationException exception) {
        ProblemDetail problemDetail =
                ProblemDetail.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .title(exception.getMessage())
                        .detail(exception.getConstraintViolations().toString())
                        .build();
        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handles AccessDeniedException.
     *
     * @return ResponseEntity
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Handles NotFoundException.
     *
     * @param exception NotFoundException
     * @return ResponseEntity
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleBadRequestException(NotFoundException exception) {
        ProblemDetail problemDetail =
                ProblemDetail.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .title(exception.getMessage())
                        .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Handles BadDataException.
     *
     * @param exception BadDataException
     * @return ResponseEntity
     */
    @ExceptionHandler(BadDataException.class)
    public ResponseEntity<ProblemDetail> handleBadRequestException(BadDataException exception) {
        ProblemDetail problemDetail =
                ProblemDetail.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .title(exception.getMessage())
                        .build();
        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Manages DataIntegrityViolationException exceptions, to provide easily readable response messages.
     * @param e exception to be managed.
     * @return Response to be sent to the user.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(
            DataIntegrityViolationException e) {
        var response =
                ResponseEntity.internalServerError()
                        .body(
                                ProblemDetail.builder()
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .title("Data integrity violation. Not saved.")
                                        .build());

        try {
            Throwable rootCause = e.getRootCause();
            if (rootCause != null) {
                String rootCauseMessage = rootCause.getMessage();
                if (rootCauseMessage != null
                        && rootCauseMessage.contains("key value violates unique constraint")) {
                    response =
                            ResponseEntity.status(409)
                                    .body(
                                            ProblemDetail.builder()
                                                    .status(HttpStatus.CONFLICT.value())
                                                    .title(
                                                            "Case-insensitive key already exists"
                                                                    + " for this type.")
                                                    .build());
                }
            }
        } catch (Exception ex) {
            return response;
        }
        return response;
    }

    /**
     * Handles all other exceptions.
     *
     * @param e Exception to capture.
     * @return Internal server error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleException(Exception e) {
        log.error("An unexpected error occurred: ", e);

        return ResponseEntity.internalServerError()
                .body(
                        ProblemDetail.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .title(
                                        "Internal server error. Please contact the system"
                                                + " administrator.")
                                .build());
    }
}
