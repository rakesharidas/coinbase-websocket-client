package com.coinbasews.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class to serialize and deserialize from json.
 */
public class JsonSerializeUtil {

    public static <T> T getTypeFromJson(String json, Class<T> clazz){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to deserialize the Json :" + json);
        }
    }

    public static String getJsonString(Object object){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return  objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize the object :" + object);
        }
    }

}
