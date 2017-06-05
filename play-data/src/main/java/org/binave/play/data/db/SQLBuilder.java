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

package org.binave.play.data.db;

import org.binave.common.util.CharUtil;

/**
 * 基本 SQL 生成工具
 *
 * @author by bin jin on 2017/6/4.
 * @since 1.8
 */
public enum SQLBuilder {

    INSERT {
        /**
         * 组装插入 SQL
         *
         * @param name      表名
         * @param fields    字段名数组
         */
        @Override
        public String getSql(String name, String[] fields) {
            return CharUtil.format(
                    "INSERT INTO {} ({}) VALUES ({})",
                    name,
                    CharUtil.join(", ", fields),
                    CharUtil.join(", ", "?", fields.length)
            );
        }
    },

    UPDATE {
        /**
         * 组装更新 SQL
         *
         * @param name      表名
         * @param fields    字段名数组
         */
        @Override
        public String getSql(String name, String[] fields) {
            return CharUtil.format(
                    "UPDATE {} SET {} WHERE ",
                    name,
                    CharUtil.join(", ", null, fields, " = ?")
            );
        }
    },

    SELECT {
        /**
         * 组装查询 SQL
         *
         * @param name      表名
         * @param fields    字段名数组
         */
        @Override
        public String getSql(String name, String[] fields) {
            return CharUtil.format(
                    "SELECT {} FROM {}",
                    CharUtil.join(", ", fields),
                    name
            );
        }
    },

    COUNT {
        /**
         * 组装计数器 SQL
         *
         * @param name      表名
         * @param fields    目标字段名称
         */
        @Override
        public String getSql(String name, String... fields) {
            return CharUtil.format(
                    "SELECT count({}) FROM {}",
                    fields[0],
                    name
            );
        }
    };

    /**
     *
     * @param name      表名
     * @param fields    字段名
     */
    abstract public String getSql(String name, String... fields);

}
