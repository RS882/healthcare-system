package com.healthcare.aiservice.common.provider;

import com.healthcare.aiservice.exception.JsonExtractorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JSON extractor tests: ")
class JsonExtractorTest {

    @Test
    void extractObject_ShouldReturnJson_WhenResponseContainsOnlyJson() {
        String rawResponse = """
                {
                  "summary": "Headache reported"
                }
                """;

        String result = JsonExtractor.extractObject(rawResponse);

        assertThat(result).isEqualTo("""
                {
                  "summary": "Headache reported"
                }
                """.strip());
    }

    @Test
    void extractObject_ShouldReturnJson_WhenResponseContainsTextBeforeAndAfterJson() {
        String rawResponse = """
                Here is the result:

                {
                  "summary": "Headache reported"
                }

                Done.
                """;

        String result = JsonExtractor.extractObject(rawResponse);

        assertThat(result).isEqualTo("""
                {
                  "summary": "Headache reported"
                }
                """.strip());
    }

    @Test
    void extractObject_ShouldThrowJsonExtractorException_WhenResponseIsNull() {
        assertThatThrownBy(() -> JsonExtractor.extractObject(null))
                .isInstanceOf(JsonExtractorException.class)
                .hasMessageContaining("AI response is empty");
    }

    @Test
    void extractObject_ShouldThrowJsonExtractorException_WhenResponseIsBlank() {
        assertThatThrownBy(() -> JsonExtractor.extractObject("   "))
                .isInstanceOf(JsonExtractorException.class)
                .hasMessageContaining("AI response is empty");
    }

    @Test
    void extractObject_ShouldThrowJsonExtractorException_WhenJsonObjectNotFound() {
        String rawResponse = """
                Here is the result:

                Symptoms:
                - Fever
                - Headache
                """;

        assertThatThrownBy(() -> JsonExtractor.extractObject(rawResponse))
                .isInstanceOf(JsonExtractorException.class)
                .hasMessageContaining("JSON object not found in AI response");
    }

    @Test
    void extractObject_ShouldReturnJson_WhenResponseContainsJsonCodeFence() {
        String rawResponse = """
            ```json
            {
              "summary": "Headache reported"
            }
            ```
            """;

        String result = JsonExtractor.extractObject(rawResponse);

        assertThat(result).isEqualTo("""
            {
              "summary": "Headache reported"
            }
            """.strip());
    }

    @Test
    void extractObject_ShouldReturnCompleteJson_WhenJsonContainsNestedObjectsAndArrays() {
        String rawResponse = """
            Here is the result:

            {
              "summary": "Headache reported",
              "medications": [
                {
                  "name": "Ibuprofen",
                  "dosage": ""
                }
              ],
              "recommendations": [
                "Follow-up"
              ]
            }
            """;

        String result = JsonExtractor.extractObject(rawResponse);

        assertThat(result).isEqualTo("""
            {
              "summary": "Headache reported",
              "medications": [
                {
                  "name": "Ibuprofen",
                  "dosage": ""
                }
              ],
              "recommendations": [
                "Follow-up"
              ]
            }
            """.strip());
    }
}