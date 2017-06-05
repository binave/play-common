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
import org.binave.common.collection.proxy.MultimapProxy;
import org.binave.play.config.util.ConfMulti;
import org.binave.play.config.args.Config;
import org.binave.play.config.args.ConfigEditor;
import org.binave.play.config.api.ConfLoader;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * Multimap 型配置管理实现
 *
 * @author bin jin on 2017/4/12.
 * @since 1.8
 */
class ConfMultiPoolImpl implements ConfMulti {

    // 全局缓存
    private static Map<String, MultimapProxy<Integer, ConfigEditor>> globalMultiMapsCache = new HashMap<>();

    private Multimap<Integer, ConfigEditor> subMultimap;

    private Source<Multimap<Integer, ConfigEditor>> source;

    private static boolean needReadLock = false;

    private static final StampedLock sl = new StampedLock();

    /**
     * @see ConfPoolFactory#createConfMulti(String)
     */
    ConfMultiPoolImpl(String token) {
        subMultimap = getSyncProxy(token);
    }

    /**
     * 刷新用
     * @see RefreshFactory#createConfMulti(Source)
     */
    ConfMultiPoolImpl(Source<Multimap<Integer, ConfigEditor>> source) {
        this.source = source;
    }

    private MultimapProxy<Integer, ConfigEditor> getSyncProxy(String token) {
        if (token == null) return null; // support update
        // 本地缓存
        return globalMultiMapsCache.computeIfAbsent(token, k -> new MultimapProxy<>()); // new 代理类，内部为空
    }

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
        return globalMultiMapsCache.size();
    }


    /**
     * 因为仅仅版本号变更，无需写时复制
     */
    private void updateVersion(long version) {

        needReadLock = true;

        for (MultimapProxy<Integer, ConfigEditor> proxy : globalMultiMapsCache.values()) {
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

    // 刷新配置
    private void reload(ConfLoader confLoader, long version, String... tokens) {

        if (source == null) throw new RuntimeException("not init Multimap source");

        if (confLoader == null || tokens == null || tokens.length == 0)
            throw new RuntimeException();

        SyncProxy[] proxies = new SyncProxy[tokens.length];
        Multimap[] multiMaps = new Multimap[tokens.length];

        for (int i = 0; i < tokens.length; i++) {

            // 拿到 Table 代理
            proxies[i] = getSyncProxy(tokens[i]);

            List<? extends ConfigEditor> configList = confLoader.loadLogicConfig(tokens[i]);

            if (configList == null || configList.isEmpty()) continue;

            multiMaps[i] = source.create();
            if (!multiMaps[i].isEmpty()) throw new RuntimeException("multiMaps not empty");

            // list -> multimap
            for (ConfigEditor con : configList) {
                con.setVersion(version);
                multiMaps[i].put(con.getKey(), con);
            }
        }

        // 将 multimap 代理放入，写时复制原生支持局部一致性
        for (int i = 0; i < tokens.length; i++) {
            if (multiMaps[i] != null) proxies[i].syncUpdate(multiMaps[i]);
        }
    }

    @Override
    public <Conf extends Config> Collection<Conf> get(int id) {
        long stamp = sl.tryOptimisticRead();
        Collection<ConfigEditor> configEditors = subMultimap.get(id);
        if (needReadLock && !sl.validate(stamp)) { // 有线程在写
            stamp = sl.readLock(); // 获得悲观读锁
            try {
                configEditors = subMultimap.get(id);
            } finally {
                sl.unlockRead(stamp); // 释放读锁
            }
        }
        return (Collection<Conf>) configEditors;
    }

    @Override
    public String toString() {
        return "ConfMultiPoolImpl{" +
                "globalMultiMapsCache=" + globalMultiMapsCache +
                ", subMultimap=" + subMultimap +
                '}';
    }
}

