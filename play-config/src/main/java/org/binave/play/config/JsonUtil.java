/*
 * Copyright (c) 2017 bin jin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.binave.play.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.binave.common.util.CharUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * json 工具类
 *
 * @author bin jin on 2017/4/18.
 * @since 1.8
 */
public class JsonUtil {

    private static Gson gson = new GsonBuilder().
            setFieldNamingStrategy(f -> {
                return f.getName().toLowerCase(); // 全部属性名小写
            }).create();

    /**
     * 从 json 字符中获得对象
     * 大小写不敏感
     *
     * @param label     标签名
     */
    public static <E> List<E> getCaseInsensitive(Class<E> type, String context, String label) {
        List<E> list = new LinkedList<>();
        // 转换成 reader
        try (StringReader reader = new StringReader(context)) {
            JsonParser parser = new JsonParser();
            JsonArray JsonContext = parser.parse(reader).
                    getAsJsonObject().getAsJsonArray(label);
            // 迭代 json 内容
            for (JsonElement element : JsonContext) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonObject newJsonObject = new JsonObject();

                // 将 key 都变为小写
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
                    newJsonObject.add(entry.getKey().toLowerCase(), entry.getValue());

                // 将 Element 转换为对象
                list.add(gson.fromJson(newJsonObject, type));
            }
        }
        return list;
    }

    /**
     * 从包含 json 的字符串中获得对象
     */
    public static <E> List<E> getCaseInsensitive(Class<E> type, File file, String label) {
        return getCaseInsensitive(type, CharUtil.readText(file), label);
    }

    /**
     * 反序列化
     */
    public static <T> T toObject(String json, Class<?>... classes) {
        if (classes == null || classes.length == 0) {
            throw new IllegalArgumentException("no class");
        }

        if (classes.length > 1) {
            Class<?> parametrized = classes[0];
            Class<?>[] copy = new Class[classes.length - 1];
            System.arraycopy(classes, 1, copy, 0, copy.length);
            JavaType type = getObjectMapper().
                    getTypeFactory().
                    constructParametricType(parametrized, copy);
            try {
                return getObjectMapper().readValue(json, type);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return (T) getObjectMapper().readValue(json, classes[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 序列化
     */
    public static String toString(Object obj) {
        try {
            return getObjectMapper().
                    writerWithDefaultPrettyPrinter(). // 格式化
                    writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true); // 忽略大小写
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 忽略 null
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY); // 忽略空字符
        return mapper;
    }

}
