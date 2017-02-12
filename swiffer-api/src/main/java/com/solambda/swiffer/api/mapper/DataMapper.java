package com.solambda.swiffer.api.mapper;

import com.solambda.swiffer.api.Input;
import com.solambda.swiffer.api.Output;

/**
 * Interface to serialize and deserialize Java objects (POJOs) from {@link Input} and {@link Output}.
 */
public interface DataMapper {

    /**
     * Serialize POJO to a String.
     *
     * @param object java object
     * @return {@code object} serialized to {@link String}, or {@code null} if {@code object} is {@code null}
     */
    String serialize(Object object);

    /**
     * Deserialize {@link String} to the object of specified class.
     *
     * @param content    string to deserialize
     * @param objectType class of the result object
     * @return object from {@code content}, or {@code null} if {@code content} is {@code null}
     */
    <T> T deserialize(String content, Class<?> objectType);
}
