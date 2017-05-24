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

package org.binave.play.data.api;

/**
 * 缓存接口
 *
 * @author bin jin on 2017/4/20.
 * @since 1.8
 */
public interface Cache {

    /**
     * 地址
     * 用于分配 pool id
     */
    String getUrl();

    /**
     * 放入
     */
    Object put(Object key, Object value);

    /**
     * 删除
     */
    Object remove(Object key);

    /**
     * 取出
     */
    <T> T get(Object key, Class<T> type);

    /**
     * 查看是否存在
     */
    boolean exist(Object key);

    /**
     * 裁剪
     *
     * @param live  留存数
     */
    void trim(int live);

}
