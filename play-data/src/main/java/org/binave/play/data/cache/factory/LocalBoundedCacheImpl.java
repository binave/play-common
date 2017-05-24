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

import org.binave.play.data.api.Adder;
import org.binave.play.data.api.Cache;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地有界限缓存
 * todo 需要修改底层代码
 *
 * @author by bin jin on 2017/5/13.
 * @since 1.8
 *
 */
public class LocalBoundedCacheImpl implements Cache, Adder {


    private Map<Object, Object> map = new ConcurrentHashMap<>();
//    private Map<Object, SortedEntry> map = new ConcurrentHashMap<>();
//    private BlockingQueue<SortedEntry> queue = new PriorityBlockingQueue<>();

    @Override
    public String getUrl() {
        // 本地缓存，不支持 池分配策略
        throw new UnsupportedOperationException();
    }

    @Override
    public Object put(Object key, Object value) {
        if (key == null || value == null) throw new NullPointerException();
        return map.put(key, value);


//        synchronized (key) {
//            SortedEntry entry = map.get(key);
//            if (entry == null) {
//                entry = new SortedEntry(key, System.currentTimeMillis(), value);
//                queue.add(entry);
//                map.put(key, entry);
//            } else {
//                entry.setTime(System.currentTimeMillis());
//                entry.setValue(value);
//                queue.remove(entry);
//                queue.add(entry);
//                // 排序
//            }
//        }
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {

//        SortedEntry entry = map.get(key);
//        if (entry == null) return null;
//        entry.setTime(System.currentTimeMillis());
//        queue.remove(entry);
//        queue.add(entry);
//        return (T) entry.getValue();
        return (T) map.get(key);
    }

    @Override
    public boolean exist(Object key) {
        return map.containsKey(key);
    }

    @Override
    public Long adder(Object key, long value) {
        return null;
    }

    @Override
    public synchronized void trim(int live) {
//        int size = map.size() - live;
//        while (size > 0) {
//            SortedEntry entry = queue.poll();
//            map.remove(entry.getKey());
//            --size;
//        }
    }

    private class SortedEntry implements Comparator<SortedEntry> {

        private Object key;
        private Long time;
        private Object value;

        SortedEntry(Object key, Long time, Object value) {
            this.key = key;
            this.time = time;
            this.value = value;
        }

        @Override
        public int compare(SortedEntry o1, SortedEntry o2) {
            // 倒序
            return (int) (o2.getTime() - o1.getTime());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SortedEntry)) return false;
            SortedEntry entry = (SortedEntry) o;
            return Objects.equals(entry.getKey(), this.getKey()) &&
                    Objects.equals(entry.getValue(), this.getValue());
        }

        @Override
        public int hashCode() {
            return getKey() != null ? getKey().hashCode() : 0;
        }

        Long getTime() {
            return time;
        }

        void setTime(Long time) {
            this.time = time;
        }

        Object getKey() {
            return this.key;
        }

        Object getValue() {
            return this.value;
        }

        void setValue(Object value) {
            this.value = value;
        }
    }
}
