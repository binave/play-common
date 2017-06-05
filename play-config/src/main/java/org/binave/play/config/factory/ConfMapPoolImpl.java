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

package org.binave.play.config.factory;

import org.binave.common.api.Source;
import org.binave.common.api.SyncProxy;
import org.binave.common.collection.proxy.MapProxy;
import org.binave.play.config.util.ConfMap;
import org.binave.play.config.args.Config;
import org.binave.play.config.args.ConfigEditor;
import org.binave.play.config.api.ConfLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * Map 型配置管理实现
 *
 * @author bin jin on 2017/4/8.
 * @since 1.8
 */
class ConfMapPoolImpl implements ConfMap {

    // 全局缓存
    private static Map<String, MapProxy<Integer, ConfigEditor>> globalMapsCache = new HashMap<>();

    private Source<Map<Integer, ConfigEditor>> source;

    // 私有缓存
    private Map<Integer, ConfigEditor> subMapCache;

    /**
     * 注册并绑定 token，注意如果不刷新，则无法使用
     * @see ConfPoolFactory#createConfMap(String)
     */
    ConfMapPoolImpl(String token) {
        // 查看全局缓存里是否存在
        subMapCache = getSyncProxy(token);
    }

    /**
     * 刷新用
     * 必须限定泛型
     * @see RefreshFactory#createConfMap(Source)
     */
    ConfMapPoolImpl(Source<Map<Integer, ConfigEditor>> source) {
        this.source = source;
    }

    private MapProxy<Integer, ConfigEditor> getSyncProxy(String token) {
        if (token == null) return null; // support update
        // 本地缓存
        return globalMapsCache.computeIfAbsent(token, k -> new MapProxy<>()); // new 代理类，内部为空
    }

    /**
     * 是否需要上锁
     */
    private static boolean needReadLock = false;
    private final static StampedLock sl = new StampedLock();


    // 刷新配置
    @Override
    public synchronized void reload(ConfLoader confLoader, long version, boolean override, String... tokens) {

        try {
            // 只是单纯的更新版本号
            if (tokens == null || tokens.length == 0) {
                updateVersion(version);
            } else {
                if (confLoader == null) throw new RuntimeException();
                reload(confLoader, version, tokens);
                updateVersion(version);
            }
        } finally {
            needReadLock = false;
        }

    }

    @Override
    public int size() {
        return globalMapsCache.size();
    }

    /**
     * 因为仅仅版本号变更，无需写时复制
     */
    private void updateVersion(long version) {
        needReadLock = true;

        for (MapProxy<Integer, ConfigEditor> proxy : globalMapsCache.values()) {
            if (proxy.isNull()) continue;
            long stamp = sl.writeLock();
            try {
                for (ConfigEditor conf : proxy.values()) {
                    // 更新旧的
                    if (conf.getVersion() != version) conf.setVersion(version);
                }
            } finally {
                sl.unlockWrite(stamp);
            }
        }
    }

    private void reload(ConfLoader confLoader, long version, String... tokens) {

        if (source == null) throw new RuntimeException("not init map source");

        SyncProxy[] proxies = new SyncProxy[tokens.length];
        Map[] maps = new Map[tokens.length];

        for (int i = 0; i < tokens.length; i++) {

            // 拿到 Map 代理
            proxies[i] = getSyncProxy(tokens[i]);

            // 从配置模块拿到配置
            List<? extends ConfigEditor> configList = confLoader.loadLogicConfig(tokens[i]);

            if (configList == null || configList.isEmpty()) continue;

            maps[i] = source.create();
            if (!maps[i].isEmpty()) throw new RuntimeException("Map no empty");

            // list -> Map
            for (ConfigEditor con : configList) {
                con.setVersion(version); // 需要把配置版本统一
                maps[i].put(con.getKey(), con);
            }
        }

        // 将 mqp 代理放入，实现全局引用替换。写时复制保证了局部一致性
        for (int i = 0; i < tokens.length; i++) {
            if (maps[i] != null) proxies[i].syncUpdate(maps[i]);
        }

    }

    @Override
    public <Conf extends Config> Conf get(int id) {
        long stamp = sl.tryOptimisticRead();
        ConfigEditor configEditor = subMapCache.get(id);
        if (needReadLock && !sl.validate(stamp)) { // 有线程在写
            stamp = sl.readLock(); // 获得悲观读锁
            try {
                configEditor = subMapCache.get(id);
            } finally {
                sl.unlockRead(stamp); // 释放读锁
            }
        }
        return (Conf) configEditor;
    }

    @Override
    public Collection<? extends Config> values() {
        long stamp = sl.tryOptimisticRead();
        Collection<? extends ConfigEditor> values = subMapCache.values();
        if (needReadLock && !sl.validate(stamp)) { // 有线程在写
            stamp = sl.readLock(); // 获得悲观读锁
            try {
                values = subMapCache.values();
            } finally {
                sl.unlockRead(stamp); // 释放读锁
            }
        }
        return values;
    }

    @Override
    public String toString() {
        return "ConfMapPoolImpl{" +
                "globalMapsCache=" + globalMapsCache +
                ", subMapCache=" + subMapCache +
                '}';
    }
}
