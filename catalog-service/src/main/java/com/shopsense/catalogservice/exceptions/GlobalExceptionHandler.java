package com.shopsense.catalogservice.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail handleDuplicateKeyException( DuplicateKeyException ex ) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Duplicate Resource", "A record with the same value already exists");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException( DataIntegrityViolationException ex ) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Data Integrity Violation", "The operation violates a database constraint");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException( MethodArgumentNotValidException ex ) {
        ProblemDetail problemDetail = buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation Failed", "One or more fields contain invalid values");
        Map<String, String> errors = new LinkedHashMap<>();
        for ( FieldError fieldError : ex.getBindingResult()
                .getFieldErrors() ) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException( ConstraintViolationException ex ) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation Failed", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException( HttpMessageNotReadableException ex ) {
        Throwable cause = ex.getCause();
        if ( cause instanceof UnrecognizedPropertyException propertyException ) {
            return buildProblemDetail(HttpStatus.BAD_REQUEST, "Unknown Field", "Unknown field: " + propertyException.getPropertyName());
        }
        if ( cause instanceof InvalidFormatException formatException ) {
            String fieldName = formatException.getPath()
                    .stream()
                    .findFirst()
                    .map(JsonMappingException.Reference::getFieldName)
                    .orElse("unknown");
            return buildProblemDetail(HttpStatus.BAD_REQUEST, "Invalid Field Format", "Invalid value for field: " + fieldName);
        }
        if ( cause instanceof MismatchedInputException ) {
            return buildProblemDetail(HttpStatus.BAD_REQUEST, "Invalid Request Body", "Request body does not match the expected format");
        }
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Malformed JSON", "Request body contains invalid JSON");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException( MethodArgumentTypeMismatchException ex ) {
        String parameterName = ex.getName();
        Object invalidValue = ex.getValue();
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType()
                .getSimpleName() : "valid type";
        String detail = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", invalidValue, parameterName, expectedType);
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Invalid Request Parameter", detail);
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ProblemDetail handleInvalidDataAccessResourceUsageException( InvalidDataAccessResourceUsageException ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Database Not Found", "An error occurred while accessing the database");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException( EntityNotFoundException ex ) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", "The requested resource was not found");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockingFailureException( OptimisticLockingFailureException ex ) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Concurrent Modification", "The resource was modified by another request. Please try again.");
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ProblemDetail handleResourceAlreadyExistsException( ResourceAlreadyExistsException ex ) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Resource Already Exists", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException( ResourceNotFoundException ex ) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleHttpRequestMethodNotSupportedException( HttpRequestMethodNotSupportedException ex ) {
        return buildProblemDetail(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "The requested HTTP method is not allowed for this resource");
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException( RuntimeException ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException( Exception ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
    }

    private ProblemDetail buildProblemDetail( HttpStatus status, String title, String detail ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}