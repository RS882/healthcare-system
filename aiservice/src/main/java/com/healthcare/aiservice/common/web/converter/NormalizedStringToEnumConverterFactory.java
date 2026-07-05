package com.healthcare.aiservice.common.web.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
public class NormalizedStringToEnumConverterFactory
        implements ConverterFactory<String, Enum> {

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return source -> convert(source, targetType);
    }

    private <T extends Enum> T convert(String source, Class<T> targetType) {
        if (!StringUtils.hasText(source)) {
            return null;
        }

        String normalized = normalize(source);

        for (T enumConstant : targetType.getEnumConstants()) {
            if (enumConstant.name().equals(normalized)) {
                return enumConstant;
            }
        }

        throw new IllegalArgumentException(
                "Invalid enum value '%s' for enum %s".formatted(
                        source,
                        targetType.getSimpleName()
                )
        );
    }

    private String normalize(String value) {
        return value.strip()
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase(Locale.ROOT);
    }
}
