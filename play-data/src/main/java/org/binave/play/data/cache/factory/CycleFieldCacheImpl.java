package org.binave.play.data.cache.factory;

import org.binave.common.util.CharUtil;
import org.binave.common.serialize.Codec;
import org.binave.common.util.FutureTime;
import org.binave.play.data.api.Cache;
import org.binave.play.data.api.Adder;
import redis.clients.jedis.Jedis;

import java.util.Objects;

/**
 * 保存自然周期内有效的缓存
 *
 * @author by bin jin on 2017/5/12.
 * @since 1.8
 */
class CycleFieldCacheImpl extends RedisLockImpl implements Cache, Adder {

    private String url;
    private Jedis redis;
    private FutureTime futureTime;
    private int index;
    private Codec codec;
    private String key;
    private byte[] keyBytes;

    CycleFieldCacheImpl(String key, Jedis redis, FutureTime futureTime, int index, Codec codec) {
        super(redis);
        this.key = key;
        this.url = redis.getClient().getHost() + redis.getDB();
        this.redis = redis;
        this.futureTime = futureTime;
        this.index = index;
        this.codec = codec;
        this.keyBytes = CharUtil.toBytes(key);
    }

    @Override
    public String getUrl() {
        return url;
    }

    /**
     * @return {@link Boolean}
     */
    @Override
    public Object put(Object field, Object value) {
        if (value instanceof Number) {
            return 0 < redis.hset(this.key, field.toString(), String.valueOf(value));
        } else {
            byte[] fieldBytes = CharUtil.toBytes(field);
            if (redis.hset(this.keyBytes, fieldBytes, codec.encode(value)) > 0) {
                // 减小设置超时时间的频率
                redis.expireAt(
                        keyBytes,
                        futureTime.getMillisecond(index, 1, true, false)
                );
                return true;
            }
            return false;
        }
    }

    /**
     * @return {@link Boolean}
     */
    @Override
    public Object remove(Object field) {
        return 1 == redis.hdel(this.keyBytes, CharUtil.toBytes(field));
    }

    @Override
    public <T> T get(Object field, Class<T> type) {
        if (String.class.equals(type)) {
            return (T) redis.hget(this.key,Objects.toString(field));
        } else if (Number.class.isAssignableFrom(type)) {
            String value = redis.hget(this.key,Objects.toString(field));
            return (T) Integer.valueOf(value.replaceAll("\\..*", ""));
        } else {
            byte[] fieldBytes = CharUtil.toBytes(field);
            byte[] values = redis.hget(keyBytes, fieldBytes);
            if (values == null) return null;
            return codec.decode(values, type);
        }
    }

    @Override
    public boolean exist(Object field) {
        return redis.hexists(keyBytes, CharUtil.toBytes(field));
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
        return "CycleFieldCacheImpl{" +
                "url='" + url + '\'' +
                ", futureTime=" + futureTime +
                ", index=" + index +
                ", codec=" + codec +
                ", key='" + key + '\'' +
                '}';
    }
}
