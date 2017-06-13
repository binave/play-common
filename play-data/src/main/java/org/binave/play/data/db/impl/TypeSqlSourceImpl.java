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

package org.binave.play.data.db.impl;

import org.binave.common.api.SourceBy;
import org.binave.common.util.TypeUtil;
import org.binave.play.data.args.Dao;
import org.binave.play.data.args.TypeSql;
import org.binave.play.data.db.SQLBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sql 生成器
 * 利用类的 {@link Dao#getParams()} 方法，生成对应 sql
 *
 * 用 类 生成 sql
 *
 * @author bin jin on 2017/4/14.
 * @since 1.8
 */
public class TypeSqlSourceImpl implements SourceBy<Class<? extends Dao>, TypeSql> {

    private Map<Class<? extends Dao>, TypeSql> sqlCache = new ConcurrentHashMap<>();

    /**
     * 通过类型，获得相关的 sql
     */
    @Override
    public TypeSql create(Class<? extends Dao> type) {
        return sqlCache.computeIfAbsent(type, k -> new SimpleTypeSqlImpl(type));
    }

    /**
     * 基本 sql 实现
     */
    private class SimpleTypeSqlImpl extends TypeSql {

        private String name; // 表名
        private String[] fields; // 表全字段
        private String insertSql;
        private String selectSql;
        private String updateSql;
        private String countSql;

        private SimpleTypeSqlImpl(Class<? extends Dao> type) {
            this.name = type.getSimpleName().toLowerCase();
            this.fields = TypeUtil.getFieldsInMethod(
                    type.getName(),
                    "getParams"
            ); // 获得 getParams 方法对应的变量

            if (fields.length == 0)
                throw new RuntimeException("can not find var: " + type.getName());

            selectSql = SQLBuilder.SELECT.getSql(name, fields);
            insertSql = SQLBuilder.INSERT.getSql(name, fields);
            updateSql = SQLBuilder.UPDATE.getSql(name, fields);
            countSql = SQLBuilder.COUNT.getSql(name, "?");
        }

        @Override
        public String getInsertSql() {
            return insertSql;
        }

        @Override
        public String getUpdateSql(String whereCondition) {
            if (whereCondition == null) throw new IllegalArgumentException();
            return updateSql + whereCondition;
        }

        @Override
        public String getSelectSql(String whereCondition) {
            if (whereCondition == null) {
                return selectSql;
            } else return selectSql + " WHERE " + whereCondition;
        }

        @Override
        public String getCountSql(String whereCondition) {
            if (whereCondition == null) {
                return countSql;
            } else return countSql + " WHERE " + whereCondition;
        }

    }
}


