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

import org.binave.util.FutureTime;
import org.binave.util.serialize.Codec;
import redis.clients.jedis.Jedis;

/**
 * 保存自然周期内有效的缓存
 *
 * @author bin jin on 2017/4/20.
 * @since 1.8
 */
class CycleCacheImpl extends BaseLockCache {

    private Jedis redis;
    private FutureTime futureTime;
    private int index;
    private Codec codec;

    CycleCacheImpl(Jedis redis, FutureTime futureTime, int index, Codec codec) {
        super(redis);

        this.redis = redis;
        this.futureTime = futureTime;
        this.codec = codec;
        this.index = index;
    }

    @Override
    public void put(String key, Object value) {

        int sec = futureTime.getSeconds(index, 1, true, true);
        if (value instanceof Number) {
            redis.setex(key, sec, String.valueOf(value));
        } else {
            byte[] keyBytes = getBytes(key);
            redis.setex(
                    keyBytes,
                    sec,
                    codec.encode(value)
            );
        }
    }

    @Override
    public void put(String key, String field, Object value) {
        if (value instanceof Number) {
            redis.hset(key, field, String.valueOf(value));
        } else {
            byte[] keyBytes = getBytes(key);
            byte[] fieldBytes = getBytes(field);
            if (redis.hset(keyBytes, fieldBytes, codec.encode(value)) > 0)
                // 减小设置超时时间的频率
                redis.expireAt(
                        keyBytes,
                        futureTime.getMillisecond(index, 1, true, false)
                );
        }
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        byte[] values = redis.get(getBytes(key));
        if (values == null) return null;
        return codec.decode(values, type);
    }

    @Override
    public <T> T get(String key, String field, Class<T> type) {
        byte[] fieldBytes = getBytes(field);
        byte[] values = redis.hget(getBytes(key), fieldBytes);
        if (values == null) return null;
        return codec.decode(values, type);
    }

    @Override
    public boolean exist(String key) {
        return redis.exists(getBytes(key));
    }

    @Override
    public boolean exist(String key, String field) {
        return redis.hexists(getBytes(key), getBytes(field));
    }

    private final static String INCR_IF_EXIST =
            "if (redis.call('exists', KEYS[1]) == 1) then " +
                    "return redis.call('incrby', KEYS[1], ARGV[1]) " +
                    "end " +
                    "return nil";

    @Override
    public Long adder(String key, long value) {
        return (Long) redis.eval(
                INCR_IF_EXIST,
                1,
                key,
                String.valueOf(value)
        );
    }

    private final static String HINCR_IF_EXIST =
            "if (redis.call('hexists', KEYS[1], ARGV[1]) == 1) then " +
                    "return redis.call('hincrby', KEYS[1], ARGV[1], ARGV[2]) " +
                    "end " +
                    "return nil";


    @Override
    public Long adder(String key, String field, long value) {
        return (Long) redis.eval(
                HINCR_IF_EXIST,
                1,
                key,
                field,
                String.valueOf(value)
        );

    }

}
