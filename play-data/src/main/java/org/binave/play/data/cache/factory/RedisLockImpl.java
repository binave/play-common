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

import org.binave.play.data.api.Lock;
import redis.clients.jedis.Jedis;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 单点分布式锁实现
 * 使用 lua 脚本保证原子性
 *
 * @author bin jin on 2017/4/21.
 * @since 1.8
 */
public class RedisLockImpl implements Lock {

    private final static long LOCK_MS = 30000; // milliseconds
    private final static int RECURSION_DEPTH_LIMIT = 30; // 递归深度限制
    private final static int MAX_SLEEP_MS = 100; // milliseconds
    private final static int MIN_SLEEP_MS = 5;
    private final static String PREFIX = "#LOCK_";
    private String tag; // 用于分类

    private Jedis redis;
    private Random random = new SecureRandom();
    private Map<Long, String> stampMap = new HashMap<>(); // 无需线程安全，获得好的性能

    RedisLockImpl(Jedis redis, String tag) {
        this.redis = redis;
        this.tag = tag;
    }

    Jedis getJedis() {
        return this.redis;
    }

    /**
     * 单点锁
     */
    @Override
    public long lock(String key) {
        int depth = 0;
        return lock(key, depth);
    }

    private long lock(String key, int depth) {

        // 注意自增，尝试次数过多，失败
        if (depth++ > RECURSION_DEPTH_LIMIT)
            throw new StackOverflowError("[lock] \'" + key +
                    "\' maximum recursion depth: " + depth);

        long stamp;
        do {
            stamp = random.nextLong();
        } while (stampMap.containsKey(stamp));

        String status = redis.set(
                PREFIX + key,
                tag + stamp,
                "NX", // Only set the key if it does not already exist
                "PX", // milliseconds
                LOCK_MS
        );

        if (!"OK".equals(status)) {
            //  没拿到 orz_
            try {
                // 休眠一个随机时间，以防和其他线程、进程同时争抢锁
                Thread.sleep(random.nextInt(MAX_SLEEP_MS - MIN_SLEEP_MS) + MIN_SLEEP_MS);
            } catch (InterruptedException ignored) {
            }

            // 再试，注意 return 为必须
            return this.lock(key, depth);
        }

        // 拿到锁了，>_<!，赶紧藏起来
        String cacheKey = stampMap.put(stamp, key);

        // 有没释放的锁头，原因不明
        if (cacheKey != null)
            System.err.println("lock() oldKey=" + cacheKey + ", value=" + key + stamp);

        return stamp;
    }

    private static final String EXPIRE_IF_EXIST_VALUE =
            "if (redis.call('get', KEYS[1]) == ARGV[1]) then " +
                    "local e = redis.call('ttl', KEYS[1]) + ARGV[2] " +
                    "return redis.call('expire', KEYS[1], e) " +
                    "end " +
                    "return 0";

    /**
     * 延长锁时间，会追加一个周期时间
     * 需要 lock 成功后立即执行
     *
     * @param stamp     锁号
     */
    @Override
    public boolean delayUnlock(long stamp) {
        String key = stampMap.get(stamp);
        if (key == null) return false;
        Object status =
//                redis.set(
//                        PREFIX + key,
//                        String.valueOf(stamp),
//                        "XX", // Only set the key if it already exist
//                        "PX", // milliseconds
//                        LOCK_MS
//                );
                redis.eval(
                        EXPIRE_IF_EXIST_VALUE,
                        1,
                        PREFIX + key,
                        tag + stamp,
                        String.valueOf(LOCK_MS / 1000) // seconds
                );
        return 1L == (Long) status;
    }

    // 如果 key 和 value 都相同则删除
    private static final String DEL_IF_EXIST_VALUE =
            "if (redis.call('get', KEYS[1]) == ARGV[1]) then " +
                    "return redis.call('del', KEYS[1]) " +
                    "end " +
                    "return nil";

    /**
     * 解锁
     */
    @Override
    public void unlock(long stamp) {
        String key = stampMap.get(stamp);
        if (key != null) {
            redis.eval(
                    DEL_IF_EXIST_VALUE,
                    1,
                    PREFIX + key,
                    tag + stamp
            );
            stampMap.remove(stamp);
        }
    }

}
