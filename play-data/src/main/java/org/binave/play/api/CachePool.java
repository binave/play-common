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

/**
 * 缓存池接口
 *
 * @author bin jin on 2017/4/18.
 * @since 1.8
 */
public interface CachePool {

    // 2017-2-01 00:00 如果时间有问题就坑爹了
    long BEGIN_TIME = 1485878400_000L;

    String SORTED_PREFIX = "SORT_";

    String HASH_PREFEX = "HASH_";

    void put(String key, Object value);

    <T> T get(String key, Class<T> type);

//    <T> Set<T> getSet(String key);

    void trim(int live);

}
