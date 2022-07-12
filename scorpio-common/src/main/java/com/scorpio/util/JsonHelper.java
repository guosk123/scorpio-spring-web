package com.scorpio.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 */
public final class JsonHelper {

    private static final boolean DEFAULT_USE_SNAKE = true;

    /**
     * Jackson Object Mapper used to serialization/deserialization
     */
    private static ObjectMapper objectMapper;

    /**
     * Naming convention between camel and underscores.
     */
    private static ObjectMapper objectMapperSnake;

    private JsonHelper() {
        throw new IllegalStateException("Utility class");
    }

    private static void initialize(boolean snake) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        mapper.registerModule(module);
        AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
        mapper.setAnnotationIntrospector(introspector);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);

        if (snake) {
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            objectMapperSnake = mapper;
        } else {
            objectMapper = mapper;
        }
    }

    /**
     * Return the ObjectMapper. It can be used to customize
     * serialization/deserialization configuration.
     *
     * @return
     */
    public static ObjectMapper getObjectMapper(boolean snake) {
        if (snake && objectMapperSnake == null) {
            initialize(true);
        } else if (!snake && objectMapper == null) {
            initialize(false);
        }
        if (snake) {
            return objectMapperSnake;
        } else {
            return objectMapper;
        }
    }

    /**
     * Serialize and object to a JSON String representation
     *
     * @param o The object to serialize
     * @return The JSON String representation
     */
    public static String serialize(Object o, boolean snake) {
        ObjectMapper mapper = getObjectMapper(snake);
        Writer writer = new StringWriter();
        try {
            mapper.writeValue(writer, o);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
        return writer.toString();
    }

    public static String serialize(Object o) {
        return serialize(o, DEFAULT_USE_SNAKE);
    }

    /**
     * 将Json数据直接输出到Writer中，减少String的内存占用情况
     *
     * @param o
     * @param writer
     */
    public static void serialize2Writer(Object o, Writer writer, boolean snake) {
        ObjectMapper mapper = getObjectMapper(snake);
        try {
            mapper.writeValue(writer, o);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    public static void serialize2Writer(Object o, Writer writer) {
        serialize2Writer(o, writer, DEFAULT_USE_SNAKE);
    }

    /**
     * Serialize and object to a JSON String representation with a Jackson view
     *
     * @param o    The object to serialize
     * @param view The Jackson view to use
     * @return The JSON String representation
     */
    public static String serialize(Object o, Class<?> view, boolean snake) {
        ObjectMapper mapper = getObjectMapper(snake);
        Writer w = new StringWriter();
        try {
            ObjectWriter writter = mapper.writerWithView(view);
            writter.writeValue(w, o);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
        return w.toString();
    }

    public static String serialize(Object o, Class<?> view) {
        return serialize(o, view, DEFAULT_USE_SNAKE);
    }

    /**
     * Deserialize a JSON string
     *
     * @param content The JSON String object representation
     * @param type    The type of the deserialized object instance
     * @return The deserialized object instance
     */
    public static <T> T deserialize(String content, Class<T> type, boolean snake) {
        ObjectMapper mapper = getObjectMapper(snake);
        try {
            return mapper.readValue(content, type);
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static <T> T deserialize(String content, Class<T> type) {
        return deserialize(content, type, DEFAULT_USE_SNAKE);
    }

    public static <T> T deserialize(InputStream inputStream, Class<T> type, boolean snake) {
        ObjectMapper mapper = getObjectMapper(snake);
        try {
            return mapper.readValue(inputStream, type);
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }


    public static <T> T deserialize(InputStream inputStream, Class<T> type) {
        return deserialize(inputStream, type, DEFAULT_USE_SNAKE);
    }

    /**
     * Deserialize a JSON string
     *
     * @param content      The JSON String object representation
     * @param valueTypeRef The typeReference containing the type of the deserialized object instance
     * @return The deserialized object instance
     */
    @SuppressWarnings("rawtypes")
    public static <T> T deserialize(String content, TypeReference valueTypeRef, boolean snake) {
        ObjectMapper mapper = getObjectMapper(snake);
        try {
            return mapper.readValue(content, valueTypeRef);
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static <T> T deserialize(String content, TypeReference valueTypeRef) {
        return deserialize(content, valueTypeRef, DEFAULT_USE_SNAKE);
    }

    @SuppressWarnings("rawtypes")
    public static <T> T deserialize(InputStream inputStream, TypeReference valueTypeRef,
                                    boolean snake) {
        ObjectMapper mapper = getObjectMapper(snake);
        try {
            return mapper.readValue(inputStream, valueTypeRef);
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static <T> T deserialize(InputStream inputStream, TypeReference valueTypeRef) {
        return deserialize(inputStream, valueTypeRef, DEFAULT_USE_SNAKE);
    }

    @SuppressWarnings("rawtypes")
    public static <T> T deserialize(JsonNode jsonNode, TypeReference valueTypeRef, boolean snake) {
        ObjectMapper mapper = getObjectMapper(snake);
        try {
            return mapper.convertValue(jsonNode, valueTypeRef);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static <T> T deserialize(JsonNode jsonNode, TypeReference valueTypeRef) {
        return deserialize(jsonNode, valueTypeRef, DEFAULT_USE_SNAKE);
    }

    public static JsonNode parseJson(String content, boolean snake) {
        ObjectMapper mapper = getObjectMapper(snake);
        try {
            return mapper.readValue(content, JsonNode.class);
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static JsonNode parseJson(String content) {
        return parseJson(content, DEFAULT_USE_SNAKE);
    }

    @SuppressWarnings("serial")
    public static class SerializationException extends RuntimeException {

        public SerializationException(Throwable cause) {
            super(cause);
        }
    }
}
