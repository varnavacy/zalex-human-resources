package com.kyriakos.compose.project.demo.zalexhumanresources.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
        Handle all the exceptions thrown manually in our service, e.g. Certification request not found
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {
        return buildResponse(ex.getStatusCode().value(), ex.getReason(), request.getRequestURI());
    }

    /*
        Thrown by Spring when the query param or path variable is of the wrong type, and cannot converted to the expected type
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected a numeric value.",
                ex.getValue(), ex.getName());
        return buildResponse(HttpStatus.BAD_REQUEST.value(), message, request.getRequestURI());
    }

    /*
        Thrown jackson when the request body cannot be desirialized, wrong type, malformed jason
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST.value(), "Invalid request body: contains malformed or incorrect field types.", request.getRequestURI());
    }

    /*
        Thrown by spring when the query param is missing
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("Required parameter '%s' is missing.", ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST.value(), message, request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(int status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status);
        body.put("message", message);
        body.put("path", path);
        return ResponseEntity.status(status).body(body);
    }
}
