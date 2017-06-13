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

package org.binave.play.data.cache.factory;

import org.binave.play.data.api.Cache;
import org.binave.common.serialize.Codec;
import org.binave.common.util.FutureTime;
import redis.clients.jedis.Jedis;

/**
 * 缓存类工厂
 *
 * @author bin jin on 2017/4/19.
 * @since 1.8
 */
public class CacheFactory {

    /**
     * 有界缓存
     * 仅仅保证最活跃的缓存
     *
     * @param key           主键
     * @param futureTime    过期周期
     * @param index         过期时刻
     * @param codec         序列化工具
     */
    public static Cache createBoundedCache(String key, Jedis redis, FutureTime futureTime, int index, Codec codec) {
        return new SortedCacheImpl(key, redis, futureTime, index, codec);
    }

    /**
     * 一维缓存
     *
     * @param futureTime    过期周期
     * @param index         过期时刻
     * @param codec         序列化工具
     */
    public static Cache createCache(Jedis redis, String tag, FutureTime futureTime, int index, Codec codec) {
        return new CycleCacheImpl(redis, tag, futureTime, index, codec);
    }

    /**
     * 二维缓存
     *
     * @param key           主键
     * @param futureTime    过期周期
     * @param index         过期时刻
     * @param codec         序列化工具
     */
    public static Cache createCache(String key, Jedis redis, String tag, FutureTime futureTime, int index, Codec codec) {
        return new CycleFieldCacheImpl(key, redis, tag, futureTime, index, codec);
    }

    /**
     * 使用本地的缓存
     */
    public static Cache createCache() {
        return new LocalBoundedCacheImpl();
    }

}
