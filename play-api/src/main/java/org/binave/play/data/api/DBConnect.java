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
 * 数据库连接器
 *
 * @author bin jin on 2017/4/13.
 * @since 1.8
 */
public interface DBConnect<D> extends DBTransact<D> {

    long getVersion();

    void setVersion(long version);

    /**
     * 增加数据
     */
    void add(D param) throws SQLException;

    /**
     * 更新数据
     */
    int update(D param) throws SQLException;


    /**
     * 查询多个
     */
    <T extends D> List<T> list(Class<T> clazz, String whereCondition) throws SQLException;


    /**
     * 查询单个
     */
    <T extends D> T get(Class<T> clazz, String whereCondition) throws SQLException;

}
