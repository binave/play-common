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

package org.binave.play.data.cache;

import org.binave.play.data.api.Cache;
import org.binave.common.serialize.Codec;
import org.binave.common.util.FutureTime;
import org.binave.play.data.api.Adder;
import redis.clients.jedis.Jedis;

/**
 * 保存自然周期内有效的缓存
 *
 * @author bin jin on 2017/4/20.
 * @since 1.8
 */
class CycleImplImpl extends RedisLockImpl implements Cache, Adder {

    private String url;
    private Jedis redis;
    private FutureTime futureTime;
    private int index;
    private Codec codec;

    CycleImplImpl(Jedis redis, FutureTime futureTime, int index, Codec codec) {
        super(redis);

        this.redis = redis;
        this.url = redis.getClient().getHost() + redis.getDB();
        this.futureTime = futureTime;
        this.codec = codec;
        this.index = index;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void put(Object key, Object value) {
        int sec = futureTime.getSeconds(index, 1, true, true);
        if (value instanceof Number) {
            redis.setex(key.toString(), sec, String.valueOf(value));
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
    public <T> T get(Object key, Class<T> type) {
        byte[] values = redis.get(getBytes(key));
        if (values == null) return null;
        return codec.decode(values, type);
    }


    @Override
    public boolean exist(Object key) {
        return redis.exists(getBytes(key));
    }

    @Override
    public void trim(int live) {
        throw new UnsupportedOperationException();
    }

    private final static String INCR_IF_EXIST =
            "if (redis.call('exists', KEYS[1]) == 1) then " +
                    "return redis.call('incrby', KEYS[1], ARGV[1]) " +
                    "end " +
                    "return nil";

    @Override
    public Long adder(Object key, long value) {
        return (Long) redis.eval(
                INCR_IF_EXIST,
                1,
                key.toString(),
                String.valueOf(value)
        );
    }

    @Override
    public String toString() {
        return "CycleImplImpl{" +
                "url='" + url + '\'' +
                ", futureTime=" + futureTime +
                ", index=" + index +
                '}';
    }
}
