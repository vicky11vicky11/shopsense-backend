package com.shopsense.notificationservice.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Tracer tracer;

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ProblemDetail handleCannotGetJdbcConnectionException( CannotGetJdbcConnectionException ex ) {
        return buildProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, "Database Unavailable",
                "Unable to connect to the database. Please try again later");
    }

    @ExceptionHandler(QueryTimeoutException.class)
    public ProblemDetail handleQueryTimeoutException( QueryTimeoutException ex ) {
        return buildProblemDetail(HttpStatus.GATEWAY_TIMEOUT, "Database Query Timeout",
                "The database took too long to respond. Please try again later");
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ProblemDetail handleSQLIntegrityConstraintViolationException( SQLIntegrityConstraintViolationException ex ) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Data Integrity Violation", extractConstraintDetail(ex));
    }

    @ExceptionHandler(SQLException.class)
    public ProblemDetail handleSQLException( SQLException ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Database Error",
                "An error occurred while accessing the database");
    }

    @ExceptionHandler(JpaSystemException.class)
    public ProblemDetail handleJpaSystemException( JpaSystemException ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Database Error",
                "An error occurred while accessing the database");
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ProblemDetail handleTransactionSystemException( TransactionSystemException ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Transaction Failed",
                "The transaction could not be completed. Please try again");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail handleDuplicateKeyException( DuplicateKeyException ex ) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Duplicate Resource", "A record with the same value already exists");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException( DataIntegrityViolationException ex ) {
        Throwable rootCause = ex.getMostSpecificCause();
        String detail = rootCause instanceof SQLIntegrityConstraintViolationException sqlEx
                ? extractConstraintDetail(sqlEx)
                : "The operation violates a database constraint";
        return buildProblemDetail(HttpStatus.CONFLICT, "Data Integrity Violation", detail);
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ProblemDetail handleInvalidDataAccessResourceUsageException( InvalidDataAccessResourceUsageException ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Database Not Found", "An error occurred while accessing the database");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockingFailureException( OptimisticLockingFailureException ex ) {
        return buildProblemDetail(HttpStatus.CONFLICT, "Concurrent Modification", "The resource was modified by another request. Please try again.");
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException( IllegalArgumentException ex ) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Invalid Request Parameter", ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleHttpRequestMethodNotSupportedException( HttpRequestMethodNotSupportedException ex ) {
        return buildProblemDetail(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "The requested HTTP method is not allowed for this resource");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException( EntityNotFoundException ex ) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Resource Not Found", "The requested resource was not found");
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException( RuntimeException ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException( Exception ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    private String extractConstraintDetail( SQLIntegrityConstraintViolationException ex ) {
        String message = ex.getMessage();
        if ( message == null ) {
            return "A record with the same value already exists";
        }
        if ( message.contains("Duplicate entry") ) {
            return "A record with the same value already exists";
        }
        if ( message.contains("a foreign key constraint fails") ) {
            return "The operation references a resource that does not exist";
        }
        return "The operation violates a database constraint";
    }

    private ProblemDetail buildProblemDetail( HttpStatus status, String title, String detail ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", Instant.now());
        Span currentSpan = tracer.currentSpan();
        if ( currentSpan != null ) {
            problemDetail.setProperty("traceId", currentSpan.context()
                    .traceId());
            problemDetail.setProperty("spanId", currentSpan.context()
                    .spanId());
        }
        return problemDetail;
    }
}