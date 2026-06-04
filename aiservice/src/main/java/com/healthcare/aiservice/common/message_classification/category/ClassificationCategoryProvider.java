package com.healthcare.aiservice.common.message_classification.category;


import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ClassificationCategoryProvider {

    public List<String> getAllowedCategories() {
        return Arrays.stream(MessageCategory.values())
                .map(Enum::name)
                .toList();
    }

    public String getAllowedCategoriesAsPromptText() {
        return getAllowedCategories()
                .stream()
                .map(category -> "- " + category)
                .reduce("", (left, right) -> left + right + System.lineSeparator());
    }
}