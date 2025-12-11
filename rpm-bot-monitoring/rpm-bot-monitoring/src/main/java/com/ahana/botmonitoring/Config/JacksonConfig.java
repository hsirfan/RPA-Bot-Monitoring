package com.ahana.botmonitoring.Config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.postConfigurer(mapper -> {
                mapper.coercionConfigFor(LogicalType.Collection)
                        .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
            });

            builder.deserializerByType(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String text = p.getText();
                    if (text == null || text.isEmpty()) {
                        return null;
                    }
                    try {
                        return LocalDateTime.parse(text);
                    } catch (DateTimeParseException e) {
                        try {
                            return LocalDate.parse(text).atStartOfDay();
                        } catch (DateTimeParseException e2) {
                            throw e;
                        }
                    }
                }
            });
        };
    }
}
