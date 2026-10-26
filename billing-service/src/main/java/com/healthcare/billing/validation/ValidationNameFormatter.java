package com.healthcare.billing.validation;

public final class ValidationNameFormatter {

    private ValidationNameFormatter() {
    }

    static String capitalizeName(String name, String defaultName) {

        if (name == null || name.isBlank()) {
            return defaultName;
        }

        String stripName = name.strip();

        return stripName.substring(0, 1).toUpperCase() + stripName.substring(1);
    }
}
