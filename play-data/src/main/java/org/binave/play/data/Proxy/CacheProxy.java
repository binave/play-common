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

package org.binave.play.data.Proxy;

import org.binave.common.api.SyncProxy;
import org.binave.play.data.api.Cache;

/**
 * 用于保证一致性 hash 在实体节点失效时，虚拟节点同时失效
 *
 * @author by bin jin on 2017/5/17.
 * @since 1.8
 */
public class CacheProxy implements Cache, SyncProxy<Cache> {

    private Cache cache;

    public CacheProxy(Cache cache) {
        this.cache = cache;
    }

    @Override
    public void syncUpdate(Cache trimCache) {
        this.cache = trimCache;
    }

    @Override
    public boolean isNull() {
        return this.cache == null;
    }

    @Override
    public String getUrl() {
        return this.cache.getUrl();
    }

    @Override
    public Object put(Object key, Object value) {
        return this.cache.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return this.cache.remove(key);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return this.cache.get(key, type);
    }

    @Override
    public boolean exist(Object key) {
        return this.cache.exist(key);
    }

    @Override
    public void trim(int live) {
        this.cache.trim(live);
    }

    @Override
    public String toString() {
        return this.cache != null ? this.cache.toString() : "NULL";
    }

    // 对比 url
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Cache && getUrl().equals(((Cache) obj).getUrl());
    }

    @Override
    public int hashCode() {
        return this.cache != null ? this.cache.hashCode() : -1;
    }

}
