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

import org.binave.play.api.SpaceConfMap;
import org.binave.play.api.config.Config;
import org.binave.play.api.config.Configure;
import org.binave.play.api.config.ConfLoader;
import org.binave.util.IndexMap;

import java.util.*;
import java.util.concurrent.locks.StampedLock;

/**
 * 配置表获取（Map）
 * 直接调用静态方法
 * 仅支持 id 具有唯一性的配置
 * 使用前需要调用 reload {@link #reload} 方法进行初始化，将所需的全部加载完成
 *
 * reload 方法用于配置刷新
 *
 * @author bin jin on 2017/3/27.
 * @since 1.8
 */
class SpaceConfMapPoolImpl implements SpaceConfMap {

    private static boolean needReadLock = false;
    private final StampedLock sl = new StampedLock();

    // 配置区间对应关系缓存
    private Map<String, SubEntry> subEntryMap = new HashMap<>();

    // 配置表缓存
    private SortedMap<Integer, Configure> globalConfigMap = new IndexMap<>();

    /**
     * 以区间为单位，进行版本号更新
     * 闭区间
     */
    private void updateVersion(long version) {

        needReadLock = true;

        for (SubEntry entry : subEntryMap.values()) {

            // 忽略版本相同的
            if (version == entry.getVersion()) continue;

            long stamp = sl.writeLock();
            try {
                // 一个区间一个区间的更新
                for (Configure conf : globalConfigMap.subMap(entry.getHead(), entry.getTail() + 1).values()) {
                    // 更新旧的
                    if (conf.getVersion() != version) conf.setVersion(version);
                }
                entry.setVersion(version);

            } finally {
                sl.unlockWrite(stamp);
            }
        }
    }


    /**
     * 覆盖配置
     * 支持一次性更新多个关联配置
     * 支持批量更新的强一致性
     * @param version  版本号
     * @param override 是否全量更新
     */
    @Override
    public void reload(ConfLoader confLoader, long version, boolean override, String... tokens) {

        try {
            if (tokens == null || tokens.length == 0) {
                updateVersion(version);
            } else {
                if (confLoader == null) throw new RuntimeException("ConfLoader is null");
                update(confLoader, version, override, tokens);

                // 如果不是全部更新
                if (!override) updateVersion(version);
            }
        } finally {
            needReadLock = false;
        }
    }


    private void update(ConfLoader confLoader, long version, boolean override, String... tokens) {

        // 新的
        SortedMap<Integer, Configure> ConfigMap = new IndexMap<>();
        Map<String, SubEntry> subEntryCache = new HashMap<>();

        // 如果不用覆盖
        if (!override) {
            ConfigMap.putAll(globalConfigMap);
            subEntryCache.putAll(subEntryMap);
        }

        boolean update = false;

        for (String token : tokens) {

            // 从其他模块逐个获得配置
            List<? extends Configure> configList = confLoader.load(token);

            // 此处不考少数无法匹配的 token
            if (configList == null || configList.isEmpty()) continue;

            update = true;

            int maxHead = Integer.MAX_VALUE, maxTail = 0;

            // 用于减少 IndexMap 增长开销，TreeMap 加入的开销可能会小一些
            SortedMap<Integer, Configure> imgMap = new TreeMap<>();

            // 转换 list 到 map
            for (Configure configure : configList) {

                // 获得两端点（最大值、最小值）闭区间
                maxHead = configure.getKey() < maxHead ? configure.getKey() : maxHead;
                maxTail = configure.getKey() > maxTail ? configure.getKey() : maxTail;

                // 设置新的版本号
                configure.setVersion(version);

                // 放入配置镜像
                imgMap.put(configure.getKey(), configure);
            }

            // 检测是否与【其他】已经存在的配置发生交集
            for (Map.Entry<String, SubEntry> entry : subEntryCache.entrySet()) {

                // 不用和自己比较
                if (token.equals(entry.getKey())) continue;

                SubEntry old = entry.getValue();

                // 测试两个范围是否存在交集
                if (intersection(maxHead, maxTail, old.getHead(), old.getTail()))
                    throw new RuntimeException("mixed token=" + token +
                            ", [" + maxHead + "-" + maxTail + "], [" + old.getHead() + "-" + old.getTail() + "]"
                    );
            }

            // 获得当前 token 旧的范围
            SubEntry old = subEntryCache.get(token);

            // 清除以前占的坑
            if (old != null) ConfigMap.subMap(old.getHead(), old.getTail() + 1).clear();

            // 放入以 token 为单位的缓存中
            subEntryCache.put(token, new SubEntry(version, maxHead, maxTail));

            // 放入全局缓存
            ConfigMap.putAll(imgMap);
        }


        if (!update) throw new RuntimeException("token is empty:" + Arrays.toString(tokens));

        needReadLock = true;

        // 上锁
        long stamp = sl.writeLock();
        try {
            globalConfigMap = ConfigMap;
            subEntryMap = subEntryCache;
        } finally {
            sl.unlockWrite(stamp);
        }


    }

    /**
     * 存储名称和范围的对应关系
     */
    private boolean intersection(int newHead, int newTail, int oldHead, int oldTail) {

        return !(oldHead == 0 && oldTail == 0) &&
                (
                        newHead >= oldHead && newTail <= oldTail ||
                                newHead <= oldHead && newTail >= oldTail ||
                                newHead <= oldHead && newTail >= oldHead ||
                                oldHead <= newTail && oldTail >= newTail
                );

    }

    /**
     * 获得配置
     */
    @Override
    public <Conf extends Config> Conf get(int id) {
        long stamp = sl.tryOptimisticRead();
        Configure configure = globalConfigMap.get(id);
        if (needReadLock && !sl.validate(stamp)) { // 有线程在写
            stamp = sl.readLock(); // 获得悲观读锁
            try {
                configure = globalConfigMap.get(id);
            } finally {
                sl.unlockRead(stamp); // 释放读锁
            }
        }
        return (Conf) configure;
    }

    /**
     * 获得配置列表
     */
    @Override
    public Collection<? extends Config> get(String token) {
        int depth = 0; // 使用局部变量，防止并发导致过早到达极限
        return get(token, depth);
    }


    private Collection<? extends Config> get(String token, int depth) {

        // 测试递归深度，注意自增
        if (depth++ > RECURSION_DEPTH_LIMIT)
            throw new StackOverflowError("get \'" + token +
                    "\' maximum recursion depth:" + depth);

        long stamp = sl.tryOptimisticRead();

        SubEntry subEntry = subEntryMap.get(token);
        Collection<? extends Config> configs = get(subEntry);

        if (needReadLock && !sl.validate(stamp)) { // 有线程在写
            stamp = sl.readLock(); // 获得悲观读锁
            try {
                subEntry = subEntryMap.get(token);
                configs = get(subEntry);
            } finally {
                sl.unlockRead(stamp); // 释放读锁
            }
        }

        // 如果不为空，则测试版本号是否一致
        if (subEntry != null && !configs.isEmpty()) {
            // 如果版本号不一致
            if (subEntry.getVersion() != configs.iterator().next().getVersion()) {
                try {
                    Thread.sleep(10); // 休眠一段时间再试
                } catch (InterruptedException ignored) {
                }
                return this.get(token, depth); // 递归调用，注意 return 为必须
            }
        }

        return configs;
    }


    private Collection<? extends Config> get(SubEntry subEntry) {
        return subEntry != null ?
                // 获得闭区间 todo 未测试区间范围
                globalConfigMap.subMap(subEntry.getHead(), subEntry.getTail() + 1).values() :
                // 无内容的集合，此处不 new 空集合
                globalConfigMap.subMap(globalConfigMap.firstKey(), globalConfigMap.firstKey()).values();
    }

    @Override
    public String toString() {
        return "SpaceConfMapPoolImpl{" +
                "globalConfigMap=" + globalConfigMap +
                ", subEntryMap=" + subEntryMap +
                '}';
    }
}
