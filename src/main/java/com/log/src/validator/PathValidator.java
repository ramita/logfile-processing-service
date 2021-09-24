package com.log.src.validator;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class PathValidator {

    public Set<String> validate(String pathToFile){
        Set<String> errors = new HashSet<>();

        return errors;
    }
}
