package com.shopsense.userservice.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.mongodb.MongoWriteException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail handleDuplicateKeyException( DuplicateKeyException ex ) {
        String message = "A record with the same value already exists";
        if ( ex.getMessage()
                .contains("email") ) {
            message = "Email already exists";
        } else if ( ex.getMessage()
                .contains("phone") ) {
            message = "Phone number already exists";
        }
        return buildProblemDetail(HttpStatus.CONFLICT, "Duplicate Resource", message);
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

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockingException( OptimisticLockingFailureException ex ) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Concurrent Modification", "The resource was modified by another request. Please try again.");
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccessException( DataAccessException ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Database Error", "An error occurred while accessing the database");
    }

    @ExceptionHandler(MongoWriteException.class)
    public ProblemDetail handleMongoWriteException( MongoWriteException ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Database Error", "An error occurred while saving data");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElementException( NoSuchElementException ex ) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", "The requested resource was not found");
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

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ProblemDetail handleResourceAlreadyExistsException( ResourceAlreadyExistsException ex ) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Resource Already Exists", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException( ResourceNotFoundException ex ) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(UserAccountBlockedException.class)
    public ProblemDetail handleUserAccountBlockedException( UserAccountBlockedException ex ) {
        return buildProblemDetail(HttpStatus.FORBIDDEN, "User Account Blocked", ex.getMessage());
    }

    @ExceptionHandler(UserAccountSuspendedException.class)
    public ProblemDetail handleUserAccountSuspendedException( UserAccountSuspendedException ex ) {
        return buildProblemDetail(HttpStatus.FORBIDDEN, "User Account Suspended", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException( RuntimeException ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException( Exception ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }


    private ProblemDetail buildProblemDetail( HttpStatus status, String title, String detail ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

}