package com.solambda.swiffer.api.mapper;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Default implementation of {@link DataMapper} which uses Jackson
 * to serialize/deserialize objects.
 */
public class JacksonDataMapper implements DataMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Default non-argument constructor.
     */
    public JacksonDataMapper() {
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public String serialize(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new DataMapperException(e);
        }
    }

    @Override
    public <T> T deserialize(String content, Class<?> objectType) {
        if (content == null) {
            return null;
        }
        try {
            return mapper.readValue(content, mapper.constructType(objectType));
        } catch (IOException e) {
            throw new DataMapperException(e);
        }
    }
}
