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

import org.binave.play.api.CachePool;
import org.binave.util.FutureTime;
import org.binave.util.serialize.Codec;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.Set;

/**
 * 常驻缓存，将保存规定序列
 *
 * @author bin jin on 2017/4/18.
 * @since 1.8
 */
class SortedCachePoolImpl implements CachePool {

    private byte[] SortedMainKey; // 有序集合
    private byte[] HashMainKey; // set 集合

    private Pipeline redis;

    private FutureTime futureTime;
    private int index;

    // 序列化反序列化工具
    private Codec codec;

    SortedCachePoolImpl(String mainKey, Pipeline pipeline, FutureTime futureTime, int index, Codec codec) {

        if (mainKey == null || mainKey.isEmpty() || pipeline == null || codec == null)
            throw new IllegalArgumentException();

        this.futureTime = futureTime;
        this.redis = pipeline;
        SortedMainKey = CycleCacheImpl.getBytes(SORTED_PREFIX + mainKey);
        HashMainKey = CycleCacheImpl.getBytes(HASH_PREFEX + mainKey);
        this.index = index;
        this.codec = codec;
    }

    // 用于缓存排序
    private long auto() {
        return System.currentTimeMillis() - BEGIN_TIME;
    }

    @Override
    public void put(String key, Object value) {
        byte[] keyBytes = CycleCacheImpl.getBytes(key);
        byte[] valueBytes = codec.encode(value);
        redis.hset(HashMainKey, keyBytes, valueBytes);
        redis.zadd(SortedMainKey, auto(), keyBytes);
        redis.sync();
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        byte[] keyBytes = CycleCacheImpl.getBytes(key);

        Response<byte[]> hgetResp = redis.hget(HashMainKey, keyBytes);
        redis.zadd(SortedMainKey, auto(), keyBytes);
        redis.sync();
        byte[] values = hgetResp.get();

        if (values == null || values.length == 0) {
            // 如果没取到，需要进行删除，有需要可以改成异步
            redis.zrem(SortedMainKey, keyBytes);
            redis.hdel(HashMainKey, keyBytes);
            redis.sync();
            return null;
        }

        // 可能插入解压或其他
        return codec.decode(values, type);
    }

    /**
     * 定时进行清理
     * 除了保留大小，其他都干掉
     */
    @Override
    public void trim(int live) {

        // 获得大小
        Response<Long> zcardResp = redis.zcard(SortedMainKey);

        // 获得过期时间
        Response<Long> ttlSortResp = redis.ttl(SortedMainKey);
        Response<Long> ttlHashResp = redis.ttl(HashMainKey);
        redis.sync();

        boolean newKey = ttlSortResp.get() < 0 || ttlHashResp.get() < 0; // -1 / -2 没有过期时间 或 没有key

        if (newKey) {
            long unixTime = futureTime.getMillisecond(index, 1, true, false);
            redis.expireAt(SortedMainKey, unixTime);
            redis.expireAt(HashMainKey, unixTime);
        }

        long size = zcardResp.get();

        // 检测长度
        if (size > live) {

            // 获得 score 比较小的，多出 liveRange 部分的列表
            Response<Set<byte[]>> zrangeResp = redis.zrange(SortedMainKey, 0, size - live);
            redis.sync();

            Set<byte[]> zrangeBytes = zrangeResp.get();
            byte[][] zrangeBytess = zrangeBytes.toArray(new byte[zrangeBytes.size()][]);

            // 删除有续集中的相应部分
            redis.zrem(SortedMainKey, zrangeBytess);
            // 删除 hash 中的相应部分
            redis.hdel(HashMainKey, zrangeBytess);
        }

        if (size > live || newKey) redis.sync();
    }


}
