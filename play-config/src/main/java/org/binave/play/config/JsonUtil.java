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

import com.google.gson.*;
import org.binave.common.util.CharUtil;

import java.io.File;
import java.io.StringReader;
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

}
