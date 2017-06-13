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

import org.binave.common.util.CharUtil;
import org.binave.play.data.api.Cache;
import org.binave.common.serialize.Codec;
import org.binave.common.util.FutureTime;
import org.binave.play.data.api.Adder;
import redis.clients.jedis.Jedis;

import java.util.Objects;

/**
 * 保存自然周期内有效的缓存
 * 所有 key 均为顶级存储
 *
 * @author bin jin on 2017/4/20.
 * @since 1.8
 */
class CycleCacheImpl extends RedisLockImpl implements Cache, Adder {

    private String url;
    private Jedis redis;
    private FutureTime futureTime;
    private int index;
    private Codec codec;

    CycleCacheImpl(Jedis redis, String tag, FutureTime futureTime, int index, Codec codec) {
        super(redis, tag);

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

    /**
     * @return {@link Boolean}
     */
    @Override
    public Object put(Object key, Object value) {
        int sec = futureTime.getSeconds(index, 1, true, true);
        if (value instanceof Number) {
            return "OK".equals(
                    redis.setex(key.toString(), sec, String.valueOf(value))
            );
        } else {
            byte[] keyBytes = CharUtil.toBytes(key);
            return "OK".equals(
                    redis.setex(
                            keyBytes,
                            sec,
                            codec.encode(value)
                    )
            );
        }
    }

    /**
     * @return {@link Boolean}
     */
    @Override
    public Object remove(Object key) {
        return 1 == redis.del(CharUtil.toBytes(key));
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        if (String.class.equals(type)) {
            return (T) redis.get(Objects.toString(key));
        } else if (Number.class.isAssignableFrom(type)) {
            String value = redis.get(Objects.toString(key));
            return (T) Integer.valueOf(value.replaceAll("\\..*", ""));
        } else {
            byte[] values = redis.get(CharUtil.toBytes(key));
            if (values == null) return null;
            return codec.decode(values, type);
        }
    }


    @Override
    public boolean exist(Object key) {
        return redis.exists(CharUtil.toBytes(key));
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
        return "CycleCacheImpl{" +
                "url='" + url + '\'' +
                ", futureTime=" + futureTime +
                ", index=" + index +
                '}';
    }
}
