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

package org.binave.play.data;

import org.binave.play.api.data.Dao;
import org.binave.util.TypeUtil;

/**
 * Sql 生成器
 * 利用类的 {@link Dao#getParams()} 方法，生成对应 sql
 *
 * @author bin jin on 2017/4/14.
 * @since 1.8
 */
class TypeSql {

    private Class type; // 表对应类型
    private String name; // 表名
    private String[] fields; // 表全字段
    private String insertSql;
    private String selectSql;
    private String updateSql;
    private String countSql;

    TypeSql(Class<? extends Dao> type) {
        this.type = type;
        this.name = type.getSimpleName().toLowerCase();
        this.fields = TypeUtil.getFieldsInMethod(
                type.getName(),
                "getParams"
        ); // 获得 getParams 方法对应的变量

        if (fields == null || fields.length == 0)
            throw new RuntimeException("can not find var: " + type.getName());

        selectSql = DBConnectFactory.getSelectSql(name, fields);
        insertSql = DBConnectFactory.getInsertSql(name, fields);
        updateSql = DBConnectFactory.getUpdateSql(name, fields);
        countSql = DBConnectFactory.getCountSql(name, "?");
    }

    String getInsertSql() {
        return insertSql;
    }

    String getUpdateSql(String whereCondition) {
        if (whereCondition == null) throw new IllegalArgumentException();
        return updateSql + whereCondition;
    }

    String getSelectSql(String whereCondition) {
        if (whereCondition == null) {
            return selectSql;
        } else return selectSql + " WHERE " + whereCondition;
    }

    String getCountSql(String whereCondition) {
        if (whereCondition == null) {
            return countSql;
        } else return countSql + " WHERE " + whereCondition;
    }

    boolean containsField(String field) {
        if (field == null || field.isEmpty()) return false;
        for (String f : fields) if (f != null && f.equalsIgnoreCase(f)) return true;
        return false;
    }

    Class getType() {
        return type;
    }

    String getName() {
        return name;
    }

    String[] getFields() {
        return fields;
    }

}


