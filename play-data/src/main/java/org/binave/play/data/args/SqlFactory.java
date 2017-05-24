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

import org.binave.play.SourceBy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过类获得基本 sql
 *
 * @author by bin jin on 2017/6/3.
 * @since 1.8
 */
public class SqlFactory<D> {

    private Map<Class, TypeSql> sqlCache = new ConcurrentHashMap<>();

    // TypeSql 源头
    private SourceBy<TypeSql, Class<? extends D>> source;

    public SqlFactory(SourceBy<TypeSql, Class<? extends D>> source) {
        this.source = source;
    }

    /**
     * 不断的生成所需的 TypeSql
     */
    private TypeSql getSqlType(Class<? extends D> type) {
        // 如果没有，则建立，否则返回缓存
        return sqlCache.computeIfAbsent(type, source::create);
    }

    public String getInsertSql(Class<? extends D> type) {
        return getSqlType(type).getInsertSql();
    }

    public String getUpdateSql(Class<? extends D> type, String whereCondition) {
        return getSqlType(type).getUpdateSql(whereCondition);
    }

    public String getSelectSql(Class<? extends D> type, String whereCondition) {
        return getSqlType(type).getSelectSql(whereCondition);
    }

    public String getCountSql(Class<? extends D> type, String whereCondition) {
        return getSqlType(type).getCountSql(whereCondition);
    }
}
