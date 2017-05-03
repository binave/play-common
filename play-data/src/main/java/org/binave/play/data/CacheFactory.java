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

package org.binave.play.data;

import org.binave.play.api.Cache;
import org.binave.play.api.CachePool;
import org.binave.util.FutureTime;
import org.binave.util.serialize.Codec;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 缓存类工厂
 *
 * @author bin jin on 2017/4/19.
 * @since 1.8
 */
public class CacheFactory {

    public static CachePool createCachePool(String mainKey, Pipeline pipeline, FutureTime futureTime, int index, Codec codec) {
        return new SortedCachePoolImpl(mainKey, pipeline, futureTime, index, codec);
    }

    /**
     * @param futureTime    过期周期
     * @param index         过期时刻
     * @param codec         序列化工具
     */
    public static Cache createCache(Jedis redis, FutureTime futureTime, int index, Codec codec) {
        return new CycleCacheImpl(redis, futureTime, index, codec);
    }

}
