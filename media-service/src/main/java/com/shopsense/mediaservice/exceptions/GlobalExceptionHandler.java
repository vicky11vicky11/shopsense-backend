package com.shopsense.mediaservice.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
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
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Malformed Request", "Request body contains invalid data");
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

    @ExceptionHandler(InvalidFileException.class)
    public ProblemDetail handleInvalidFileException( InvalidFileException ex ) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, "Invalid File", ex.getMessage());
    }

    @ExceptionHandler(MediaNotFoundException.class)
    public ProblemDetail handleMediaNotFoundException( MediaNotFoundException ex ) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, "Media Not Found", ex.getMessage());
    }

    @ExceptionHandler(MediaUploadException.class)
    public ProblemDetail handleMediaUploadException( MediaUploadException ex ) {
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Media Operation Failed", "Unable to complete the media operation");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleHttpRequestMethodNotSupportedException( HttpRequestMethodNotSupportedException ex ) {
        return buildProblemDetail(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "The requested HTTP method is not allowed for this resource");
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