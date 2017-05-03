package org.binave.play.data.cache;

import org.binave.play.data.api.Cache;
import org.binave.common.serialize.Codec;
import org.binave.common.util.FutureTime;
import org.binave.play.data.api.Adder;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

/**
 * 保存自然周期内有效的缓存
 *
 * @author by bin jin on 2017/5/12.
 * @since 1.8
 */
class CycleFieldCycleImpl extends RedisLockImpl implements Cache, Adder {

    private String url;
    private Jedis redis;
    private FutureTime futureTime;
    private int index;
    private Codec codec;
    private String key;
    private byte[] keyBytes;

    CycleFieldCycleImpl(String key, Jedis redis, FutureTime futureTime, int index, Codec codec) {
        super(redis);
        this.key = key;
        this.url = redis.getClient().getHost() + redis.getDB();
        this.redis = redis;
        this.futureTime = futureTime;
        this.index = index;
        this.codec = codec;
        this.keyBytes = getBytes(key);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void put(Object field, Object value) {
        if (value instanceof Number) {
            redis.hset(this.key, field.toString(), String.valueOf(value));
        } else {
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
    public <T> T get(Object field, Class<T> type) {
        byte[] fieldBytes = getBytes(field);
        byte[] values = redis.hget(keyBytes, fieldBytes);
        if (values == null) return null;
        return codec.decode(values, type);
    }

    @Override
    public boolean exist(Object field) {
        return redis.hexists(keyBytes, getBytes(field));
    }

    @Override
    public void trim(int live) {
        throw new UnsupportedOperationException();
    }

    private final static String HINCR_IF_EXIST =
            "if (redis.call('hexists', KEYS[1], ARGV[1]) == 1) then " +
                    "return redis.call('hincrby', KEYS[1], ARGV[1], ARGV[2]) " +
                    "end " +
                    "return nil";


    @Override
    public Long adder(Object field, long value) {
        return (Long) redis.eval(
                HINCR_IF_EXIST,
                1,
                this.key,
                field.toString(),
                String.valueOf(value)
        );

    }

    @Override
    public String toString() {
        return "CycleFieldCycleImpl{" +
                "url='" + url + '\'' +
                ", futureTime=" + futureTime +
                ", index=" + index +
                ", codec=" + codec +
                ", key='" + key + '\'' +
                '}';
    }
}
