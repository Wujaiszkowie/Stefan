package com.wspiernik.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wspiernik.infrastructure.llm.dto.LlmMessage;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter
public class JsonToLlmMessageListConverter implements AttributeConverter<List<LlmMessage>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(final List<LlmMessage> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Nie można przekonwertować listy do JSON", e);
        }

    }

    @Override
    public List<LlmMessage> convertToEntityAttribute(final String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<LlmMessage>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Nie można sparsować JSON do listy", e);
        }
    }
}
