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

import java.sql.SQLException;
import java.util.List;

/**
 * 数据库连接器事务版本
 *
 * @author by bin jin on 2017/5/12.
 * @since 1.8
 */
public interface DBTransact<D> {

    /**
     * 锁，默认锁行
     */
    long lock();

    /**
     * 解锁提交或
     *
     * @param stamp     {@link #lock()} 返回值
     */
    void unlock(long stamp);

    /**
     * 回滚
     */
    void rollback(long stamp);

    int add(long stamp, D... params) throws SQLException;

    int update(long stamp, D param) throws SQLException;

    <T extends D> List<T> list(long stamp, Class<T> clazz, String whereCondition) throws SQLException;

    <T extends D> T get(long stamp, Class<T> clazz, String whereCondition) throws SQLException;

}
