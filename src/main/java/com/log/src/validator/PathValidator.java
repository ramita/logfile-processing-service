package com.log.src.validator;

import com.log.src.constant.Error;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
public class PathValidator {

    /**
     * Validate path of file.
     * @param filename
     * @return
     * @throws MalformedURLException
     */
    public Set<String> validate(String filename) throws MalformedURLException {
        Set<String> errors = new HashSet<>();
        Path file = Paths.get(filename);
        if (Objects.isNull(file)) {
            errors.add(Error.FILE_PATH_INVALID.msg);
        }

        return errors;
    }
}
