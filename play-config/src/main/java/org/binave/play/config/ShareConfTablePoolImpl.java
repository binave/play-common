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
import org.binave.common.collection.proxy.TableProxy;
import org.binave.play.config.api.ShareConfTable;
import org.binave.play.config.args.Config;
import org.binave.play.config.args.Configure;
import org.binave.play.config.api.ConfLoader;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

/**
 * 配置表获取（Table）
 * 仅支持 id 重复的配置，
 * 需要在实例化时指定要获取的类型名，
 *
 * 其中 {@link #reload} 方法负责中途更新和初始化。
 *
 * @author bin jin on 2017/4/7.
 * @since 1.8
 */
class ShareConfTablePoolImpl implements ShareConfTable {

    // 全局缓存
    private static Map<String, TableProxy<Integer, Integer, Configure>> globalTablesCache = new HashMap<>();

    // 私有缓存
    private Table<Integer, Integer, Configure> subTableCache;

    // 注册并绑定 token，注意如果不刷新，则无法使用
    ShareConfTablePoolImpl(String token) {
        // 查看全局缓存里是否存在
        subTableCache = getSyncProxy(token);

    }

    private TableProxy<Integer, Integer, Configure> getSyncProxy(String token) {
        if (token == null) return null; // support update

        TableProxy<Integer, Integer, Configure> syncProxy = globalTablesCache.get(token);
        if (syncProxy == null) {
            syncProxy = new TableProxy<>(); // new 代理类，内部为空
            globalTablesCache.put(token, syncProxy); // 本地缓存
        }
        return syncProxy;
    }

    private static boolean needReadLock = false;
    private static final StampedLock sl = new StampedLock();

    @Override
    public void reload(ConfLoader confLoader, long version, boolean override, String... tokens) {
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
        for (TableProxy<Integer, Integer, Configure> proxy : globalTablesCache.values()) {
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


    /**
     * 刷新配置
     *
     * 请保证只有一个引用用来执行。
     * synchronized 保证 {@link #needReadLock } 逻辑的正确
     *
     * @param version  版本号
     */
    public void reload(ConfLoader confLoader, long version, String... tokens) {

        if (confLoader == null || tokens == null || tokens.length == 0)
            throw new RuntimeException();

        SyncProxy[] proxies = new SyncProxy[tokens.length];
        Table[] tables = new Table[tokens.length];

        for (int i = 0; i < tokens.length; i++) {

            // 拿到 Table 代理，进行赋值。如果是新增配置，旧的也拿不到，保证了一致性
            proxies[i] = getSyncProxy(tokens[i]);

            // 从配置模块获得配置
            List<? extends Configure> confList = confLoader.loadLogicConfig(tokens[i]);

            if (confList == null || confList.isEmpty()) continue;

            tables[i] = HashBasedTable.create(); // 空 table

            // list -> table
            for (Configure con : confList) {
                if (con.getExtKey() != -1) {// 不支持扩展 key
                    con.setVersion(version);
                    tables[i].put(con.getKey(), con.getExtKey(), con);
                }
            }
        }

        // 将 table 放入代理，无需考虑局部一致性
        for (int i = 0; i < tokens.length; i++) {
            if (tables[i] != null) proxies[i].syncUpdate(tables[i]);
        }

    }

    @Override
    public <Conf extends Config> Conf get(int id, int extId) {
        long stamp = sl.tryOptimisticRead(); // 这个不需要关闭
        Configure configure = subTableCache.get(id, extId);
        if (needReadLock && !sl.validate(stamp)) { // 有线程在写
            stamp = sl.readLock(); // 获得悲观读锁
            try {
                configure = subTableCache.get(id, extId);
            } finally {
                sl.unlockRead(stamp); // 释放读锁
            }
        }
        return (Conf) configure;
    }

    @Override
    public Map<Integer, ? extends Config> row(int rowKey) {
        long stamp = sl.tryOptimisticRead();
        Map<Integer, ? extends Configure> row = subTableCache.row(rowKey);
        if (needReadLock && !sl.validate(stamp)) {
            stamp = sl.readLock();
            try {
                row = subTableCache.row(rowKey);
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return row;
    }

    @Override
    public String toString() {
        return "ShareConfTablePoolImpl{" +
                "globalTablesCache=" + globalTablesCache +
                ", subTableCache=" + subTableCache +
                '}';
    }
}
