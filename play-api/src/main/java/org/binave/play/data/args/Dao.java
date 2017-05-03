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

package org.binave.play.data.args;

/**
 * 数据库对象接口
 *
 * [module args]
 *
 * @author bin jin on 2017/4/11.
 * @since 1.8
 */
public abstract class Dao {

    /**
     * 获得全局 ID，
     * 由全局 id 分配逻辑进行分配
     * 额外支持灰度
     */
    abstract public long getId();

    /**
     * 由全局 id 分配逻辑进行调用
     */
    abstract public void setId(long id);

    /**
     * 获得逻辑 pool ID
     * 用于分配数据库，
     * 需要请求进行携带，以支持无状态
     */
    abstract public int getPool();

    /**
     * 由 pool 分配逻辑调用
     */
    abstract public void setPool(int pool);

    /**
     * 用于 {@link java.util.Map} 索引
     */
    @Override
    public int hashCode() {
        return Long.hashCode(getId());
    }

    /**
     * 用于 {@link java.util.Map} 索引
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Access && getId() == ((Access) obj).getId();
    }

    /**
     * 获得属性参数
     * 用于数据库传参
     *
     * e.g. new Object[]{id, name, age, sex, data};
     */
    abstract public Object[] getParams();

}
