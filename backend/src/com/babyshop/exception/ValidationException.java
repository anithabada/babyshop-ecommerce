package com.babyshop.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thrown when request-body validation fails. Carries a map of
 * field -> error message so the frontend can show inline errors,
 * mirroring how Spring's @Valid + MethodArgumentNotValidException works.
 */
public class ValidationException extends ApiException {
    private final Map<String, String> fieldErrors;

    public ValidationException(Map<String, String> fieldErrors) {
        super(400, "Validation failed");
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public static class Builder {
        private final Map<String, String> errors = new LinkedHashMap<>();

        public Builder require(String field, String value, String message) {
            if (value == null || value.isBlank()) {
                errors.put(field, message);
            }
            return this;
        }

        public Builder check(String field, boolean valid, String message) {
            if (!valid && !errors.containsKey(field)) {
                errors.put(field, message);
            }
            return this;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public void throwIfInvalid() {
            if (hasErrors()) {
                throw new ValidationException(errors);
            }
        }
    }
}
