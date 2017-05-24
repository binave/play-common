package org.binave.play.data.cache;

import org.binave.common.util.CharUtil;
import org.binave.common.util.DataUtil;
import org.binave.play.data.Proxy.CacheProxy;
import org.binave.play.data.api.Cache;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 基于一致性 hash 的缓存池
 *
 * todo 需要进行边界测试
 * @author by bin jin on 2017/5/17.
 * @since 1.8
 */
public class ConsistentCachePool {

    private final static int IMG_COUNT = 160;

    /**
     * hash 方式
     */
    private CharUtil.ConsistentHash consistentHash;

    public ConsistentCachePool(CharUtil.ConsistentHash consistentHash) {
        this.consistentHash = consistentHash;
    }

    /**
     * pool
     */
    private SortedMap<Long, CacheProxy> proxyCachePool;

    /**
     * 添加节点
     * 首次运行前，需要添加节点
     * 覆盖或更新
     *
     * @param override  是否覆盖
     */
    public synchronized void put(boolean override, Cache... caches) {
        SortedMap<Long, CacheProxy> newCachePool = getProxyCachePool(override);
        for (Cache cache : caches) {
            for (int i = 0; i < IMG_COUNT; i++) {
                byte[] kay = CharUtil.toBytes(cache.getUrl() + "." + i);
                newCachePool.put(
                        consistentHash.hash(kay, 0, kay.length),
                        new CacheProxy(cache)
                );
            }
        }
        this.proxyCachePool = newCachePool;
    }

    /**
     * 同上
     */
    public synchronized void put(boolean override, Collection<Cache> caches) {
        SortedMap<Long, CacheProxy> newCachePool = getProxyCachePool(override);
        for (Cache cache : caches) {
            for (int i = 0; i < IMG_COUNT; i++) {
                byte[] kay = CharUtil.toBytes(cache.getUrl() + "." + i);
                newCachePool.put(
                        consistentHash.hash(kay, 0, kay.length),
                        new CacheProxy(cache)
                );
            }
        }
        this.proxyCachePool = newCachePool;
    }

    /**
     * 拿到一个 SortedMap
     * @param override 是否包含原有的节点
     */
    private SortedMap<Long, CacheProxy> getProxyCachePool(boolean override) {
        SortedMap<Long, CacheProxy> newCachePool = new TreeMap<>();
        if (!override && this.proxyCachePool != null) {
            // 如果不是覆盖更新，并且缓存不为 null
            newCachePool.putAll(this.proxyCachePool);
            // 清除没用的节点
            newCachePool.values().removeIf(CacheProxy::isNull);
        }
        return newCachePool;
    }

    /**
     * 从 hash 环中获得链接
     */
    public Cache getPoolCache(long id) {

        byte[] keyBytes = new byte[DataUtil.LONG_FACTOR];

        Long key = consistentHash.hash(
                DataUtil.writeLong(keyBytes, 0, id),
                0,
                DataUtil.LONG_FACTOR
        );

        // 获得 range 区间，左闭右开
        SortedMap<Long, CacheProxy> tailRemoteCachePool = proxyCachePool.tailMap(key);

        // 已经是 hash 环的尾部了
        if (tailRemoteCachePool.isEmpty()) {
            CacheProxy trimCache = proxyCachePool.get(proxyCachePool.firstKey());

            // 虚拟节点已经失效
            if (trimCache.isNull()) {

                // 准备使用写时复制清除虚拟节点
                SortedMap<Long, CacheProxy> newRemoteCachePool = new TreeMap<>(proxyCachePool);

                // 删除所有空的虚拟节点
                newRemoteCachePool.values().removeIf(CacheProxy::isNull);

                // 如果都失效了
                if (newRemoteCachePool.isEmpty()) throw new RuntimeException(
                        "Cache is empty"
                );

                // 赋值
                proxyCachePool = newRemoteCachePool;

                // 重新拿取，todo 暂时不处理节点继续失效的问题，可以用递归
                SortedMap<Long, CacheProxy> tailMap = newRemoteCachePool.tailMap(key);

                return tailMap.get(tailMap.firstKey());

            } else return trimCache;

            // 找到第一个匹配的 key
        } else return tailRemoteCachePool.get(tailRemoteCachePool.firstKey());
    }

    /**
     * 进行 trim
     */
    public void trim(int live) {

        SortedMap<Long, CacheProxy> newRemoteCachePool = new TreeMap<>(proxyCachePool);

        Iterator<CacheProxy> proxyIterator = newRemoteCachePool.values().iterator();

        while (proxyIterator.hasNext()) {
            CacheProxy proxy = proxyIterator.next();
            if (proxy.isNull()) {
                proxyIterator.remove(); // 干掉无效节点 todo 可能需要考虑重试回复问题
            } else proxy.trim(live); // 进行整理
        }

        // 赋值
        proxyCachePool = newRemoteCachePool;

    }

    @Override
    public String toString() {
        return "ConsistentCachePool{" +
                "proxyCachePool=" + proxyCachePool +
                '}';
    }

    public int size() {
        return proxyCachePool != null ? proxyCachePool.size() : 0;
    }
}
