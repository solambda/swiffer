package com.solambda.swiffer.api.mapper;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;


public class JacksonDataMapperTest {

    private final JacksonDataMapper jacksonDataMapper = new JacksonDataMapper();

    /**
     * Test case: complex object is serialized and deserialized producing the same result.
     */
    @Test
    public void complexObjectTwoWay() {
        ComplexJavaObject complexJavaObject = new ComplexJavaObject("My String Value", 78.90f);
        complexJavaObject.setIntArray(new int[]{3, 4, 5, 6});
        complexJavaObject.setComplexList(Arrays.asList(new ComplexJavaObject("Inner 1", 1),
                                                       new ComplexJavaObject("Inner 2", 78L)));

        String serialization = jacksonDataMapper.serialize(complexJavaObject);
        ComplexJavaObject deserialization = jacksonDataMapper.deserialize(serialization, ComplexJavaObject.class);

        assertThat(deserialization).isEqualTo(deserialization);
    }

    @Test
    public void serialize(){
        String expected = "{\"finalStringValue\":\"This is string\"," +
                "\"finalNumberValue\":1.0E-4," +
                "\"complexList\":[{\"finalStringValue\":\"Inner\",\"finalNumberValue\":1,\"complexList\":null,\"intArray\":null,\"alwaysNull\":null}]," +
                "\"intArray\":[3,4]," +
                "\"alwaysNull\":null}";
        ComplexJavaObject complexJavaObject = new ComplexJavaObject("This is string", 0.0001);
        complexJavaObject.setIntArray(new int[]{3, 4});
        complexJavaObject.setComplexList(Collections.singletonList(new ComplexJavaObject("Inner", 1)));

        String serialized = jacksonDataMapper.serialize(complexJavaObject);

        assertThat(serialized).isEqualTo(expected);
    }


    @Test
    public void serialize_null() throws Exception {
        String result = jacksonDataMapper.serialize(null);

        assertThat(result).isNull();
    }

    @Test
    public void serialize_empty() throws Exception {
        String result = jacksonDataMapper.serialize("");

        assertThat(result).isEqualTo("\"\"");
    }

    @Test
    public void deserialize() throws Exception {
        String expected = "This is string";

        String deserialized = jacksonDataMapper.deserialize("\"" + expected + "\"", String.class);

        assertThat(deserialized).isEqualTo(expected);
    }

    @Test
    public void deserialize_null() throws Exception {
        Integer integer = jacksonDataMapper.deserialize(null, Integer.class);

        assertThat(integer).isNull();
    }

    @Test
    public void deserialize_empty() throws Exception {
        String object = jacksonDataMapper.deserialize("\"\"", String.class);

        assertThat(object).isEmpty();
    }
}