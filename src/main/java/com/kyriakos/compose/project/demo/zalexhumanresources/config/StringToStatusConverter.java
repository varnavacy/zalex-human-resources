package com.kyriakos.compose.project.demo.zalexhumanresources.config;

import com.kyriakos.compose.project.demo.zalexhumanresources.model.Status;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Component
public class StringToStatusConverter implements Converter<String, Status> {

    @Override
    public Status convert(String value) {
        try {
            return Status.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status value: " + value + ". Valid values are: " + Arrays.toString(Status.values()));
        }
    }
}
