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

import org.binave.common.util.CharUtil;
import org.binave.common.util.TypeUtil;
import org.binave.play.data.args.Dao;
import org.binave.play.data.args.TypeSql;
import org.binave.play.SourceBy;

/**
 * Sql 生成器
 * 利用类的 {@link Dao#getParams()} 方法，生成对应 sql
 *
 * @author bin jin on 2017/4/14.
 * @since 1.8
 */
public class TypeSqlSourceImpl implements SourceBy<TypeSql, Class<? extends Dao>> {

    /**
     * 通过类型，获得相关的 sql
     */
    @Override
    public TypeSql create(Class<? extends Dao> type) {
        return new SimpleTypeSqlImpl(type);
    }

    /**
     * 基本 sql 实现
     */
    private class SimpleTypeSqlImpl implements TypeSql {

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

            if (fields == null || fields.length == 0)
                throw new RuntimeException("can not find var: " + type.getName());

            selectSql = getSelectSql(name, fields);
            insertSql = getInsertSql(name, fields);
            updateSql = getUpdateSql(name, fields);
            countSql = getCountSql(name, "?");
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

        /**
         * 组装插入 SQL
         *
         * @param name      表名
         * @param fields    字段名数组
         */
        private String getInsertSql(String name, String[] fields) {
            return CharUtil.format(
                    "INSERT INTO {} ({}) VALUES ({})",
                    name,
                    CharUtil.join(", ", null, fields, null),
                    CharUtil.join(", ", "?", fields.length)
            );
        }

        /**
         * 组装更新 SQL
         *
         * @param name      表名
         * @param fields    字段名数组
         */
        private String getUpdateSql(String name, String[] fields) {
            return CharUtil.format(
                    "UPDATE {} SET {} WHERE ",
                    name,
                    CharUtil.join(", ", null, fields, " = ?")
            );
        }

        /**
         * 组装查询 SQL
         *
         * @param name      表名
         * @param fields    字段名数组
         */
        private String getSelectSql(String name, String[] fields) {
            return CharUtil.format(
                    "SELECT {} FROM {}",
                    CharUtil.join(", ", null, fields, null),
                    name
            );
        }

        /**
         * 组装计数器 SQL
         *
         * @param name      表名
         * @param field     目标字段名称
         */
        private String getCountSql(String name, String field) {
            return CharUtil.format(
                    "SELECT count({}) FROM {}",
                    field,
                    name
            );
        }
    }
}


