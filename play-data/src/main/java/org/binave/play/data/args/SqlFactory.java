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

import org.binave.common.api.Mixture;
import org.binave.common.api.SourceBy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过类获得基本 sql
 *
 * @author by bin jin on 2017/6/3.
 * @since 1.8
 */
public class SqlFactory<D> {

    private Map<Mixture<Class<? extends D>>, TypeSql> sqlCache = new ConcurrentHashMap<>();

    // TypeSql 源头
    private SourceBy<Class<? extends D>, TypeSql> source;

    public SqlFactory(SourceBy<Class<? extends D>, TypeSql> source) {
        this.source = source;
    }

    /**
     * 不断的生成所需的 TypeSql
     */
    @SafeVarargs
    private final TypeSql getSqlType(Class<? extends D>... types) {
        Mixture<Class<? extends D>> mixture = new Mixture<>(types);
        // 如果没有，则建立，否则返回缓存
        return sqlCache.computeIfAbsent(mixture, m -> source.create(m));
    }

    @SafeVarargs
    public final String getInsertSql(Class<? extends D>... types) {
        return getSqlType(types).getInsertSql();
    }

    @SafeVarargs
    public final String getUpdateSql(String whereCondition, Class<? extends D>... types) {
        return getSqlType(types).getUpdateSql(whereCondition);
    }

    @SafeVarargs
    public final String getSelectSql(String whereCondition, Class<? extends D>... types) {
        return getSqlType(types[0]).getSelectSql(whereCondition);
    }

    @SafeVarargs
    public final String getCountSql(String whereCondition, Class<? extends D>... types) {
        return getSqlType(types).getCountSql(whereCondition);
    }
}
