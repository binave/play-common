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

package org.binave.play.api.data;

import java.sql.SQLException;
import java.util.List;

/**
 * 数据库连接器
 *
 * @author bin jin on 2017/4/13.
 * @since 1.8
 */
public interface DBConnect {

    long getVersion();

    void setVersion(long version);

    /**
     * 锁，默认锁行
     */
    long lock();

    void unlock(long stamp, boolean rollback);

    /**
     * 增加数据
     */
    void add(Dao param) throws SQLException;

    void add(long stamp, Dao param) throws SQLException;

    /**
     * 更新数据
     */
    int update(Dao param) throws SQLException;

    int update(long stamp, Dao param) throws SQLException;

//    /**
//     * 批量插入
//     */
//    int[] batch( Dao[] params) throws SQLException;
//
//    int[] batch(long stamp, Dao[] params) throws SQLException;

    /**
     * 查询多个
     */
    <T extends Dao> List<T> list(Class<T> clazz, String whereCondition) throws SQLException;

    <T extends Dao> List<T> list(long stamp, Class<T> clazz, String whereCondition) throws SQLException;

    /**
     * 查询单个
     */
    <T extends Dao> T get(Class<T> clazz, String whereCondition) throws SQLException;

    <T extends Dao> T get(long stamp, Class<T> clazz, String whereCondition) throws SQLException;
}
