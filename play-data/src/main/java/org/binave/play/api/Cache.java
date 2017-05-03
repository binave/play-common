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

package org.binave.play.api;


import org.binave.play.api.data.Lock;

/**
 * 缓存接口
 *
 * @author bin jin on 2017/4/20.
 * @since 1.8
 */
public interface Cache extends Lock {

    /**
     * 放入 redis 中
     */
    void put(String key, Object value);

    /**
     * 放入 hash 中
     */
    void put(String key, String field, Object value);

    <T> T get(String key, Class<T> type);

    <T> T get(String key, String field, Class<T> type);

    boolean exist(String key);

    boolean exist(String key, String field);

    /**
     * 拿到全局唯一的数值
     *
     * 在有值的情况下，才会累加
     */
    Long adder(String key, long value);

    Long adder(String key, String field, long value);

}
