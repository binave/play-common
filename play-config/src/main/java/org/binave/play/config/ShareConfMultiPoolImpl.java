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

package org.binave.play.config;

import org.binave.common.collection.SyncProxy;
import org.binave.common.collection.proxy.MultimapProxy;
import org.binave.play.config.api.ShareConfMulti;
import org.binave.play.config.args.Config;
import org.binave.play.config.args.Configure;
import org.binave.play.config.api.ConfLoader;
import com.google.common.collect.ArrayListMultimap;
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
class ShareConfMultiPoolImpl implements ShareConfMulti {

    // 全局缓存
    private static Map<String, MultimapProxy<Integer, Configure>> globalMultiMapsCache = new HashMap<>();

    private Multimap<Integer, Configure> subMultimap;

    ShareConfMultiPoolImpl(String token) {
        subMultimap = getSyncProxy(token);
    }

    private MultimapProxy<Integer, Configure> getSyncProxy(String token) {
        if (token == null) return null; // support update

        MultimapProxy<Integer, Configure> syncProxy = globalMultiMapsCache.get(token);
        if (syncProxy == null) {

            // new 代理类，内部为空
            syncProxy = new MultimapProxy<>();

            // 本地缓存
            globalMultiMapsCache.put(token, syncProxy);
        }
        return syncProxy;
    }

    private static boolean needReadLock = false;
    private static final StampedLock sl = new StampedLock();


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


    /**
     * 因为仅仅版本号变更，无需写时复制
     */
    private void updateVersion(long version) {

        needReadLock = true;

        for (MultimapProxy<Integer, Configure> proxy : globalMultiMapsCache.values()) {
            if (proxy.isNull()) continue;
            long stamp = sl.writeLock();
            try {
                for (Configure conf : proxy.values()) {
                    // 更新旧的
                    if (conf.getVersion() != version) conf.setVersion(version);
                }
            } finally {
                sl.unlockWrite(stamp);
            }
        }
    }

    // 刷新配置
    public void reload(ConfLoader confLoader, long version, String... tokens) {

        if (confLoader == null || tokens == null || tokens.length == 0)
            throw new RuntimeException();

        SyncProxy[] proxies = new SyncProxy[tokens.length];
        Multimap[] multiMaps = new Multimap[tokens.length];

        for (int i = 0; i < tokens.length; i++) {

            // 拿到 Table 代理
            proxies[i] = getSyncProxy(tokens[i]);

            List<? extends Configure> configList = confLoader.loadLogicConfig(tokens[i]);

            if (configList == null || configList.isEmpty()) continue;

            multiMaps[i] = ArrayListMultimap.create();

            // list -> multimap
            for (Configure con : configList) {
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
        Collection<Configure> configures = subMultimap.get(id);
        if (needReadLock && !sl.validate(stamp)) { // 有线程在写
            stamp = sl.readLock(); // 获得悲观读锁
            try {
                configures = subMultimap.get(id);
            } finally {
                sl.unlockRead(stamp); // 释放读锁
            }
        }
        return (Collection<Conf>) configures;
    }

    @Override
    public String toString() {
        return "ShareConfMultiPoolImpl{" +
                "globalMultiMapsCache=" + globalMultiMapsCache +
                ", subMultimap=" + subMultimap +
                '}';
    }
}

