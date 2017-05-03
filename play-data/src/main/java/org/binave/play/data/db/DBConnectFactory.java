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
import org.binave.play.data.args.DBConfig;
import org.binave.play.data.api.DBConnect;
import org.binave.play.data.args.Dao;
import org.binave.play.data.db.DBConnectImpl;
import org.binave.play.data.db.SimpleDBConfigImpl;

import java.sql.SQLException;
import java.util.Map;

/**
 * 数据库链接工厂
 *
 * @author bin jin on 2017/4/19.
 * @since 1.8
 */
public class DBConnectFactory {

    /**
     * 获得基础数据库工具
     */
    public static DBConnect<Dao> createDBConnect(DBConfig dbConfig) {
        try {
            return new DBConnectImpl(dbConfig);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static DBConnect<Dao> createDBConnect(String jdbcUrl) {
        return createDBConnect(getSimpleDataSource(jdbcUrl));
    }

    /**
     * 从 url 中获得数据库链接条件
     */
    private static DBConfig getSimpleDataSource(String jdbcUrl) {

        SimpleDBConfigImpl basicDataSource = new SimpleDBConfigImpl();

        String driverClassName;

        if (jdbcUrl.startsWith("jdbc:mysql:"))
            driverClassName = "com.mysql.jdbc.Driver";
        else if (jdbcUrl.startsWith("jdbc:oracle:"))
            driverClassName = "oracle.jdbc.driver.OracleDriver";
        else if (jdbcUrl.startsWith("jdbc:sqlserver:"))
            driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        else throw new RuntimeException("jdbc driver class name is empty: " + jdbcUrl);

        Map<String, String> jdbcMap = CharUtil.getParameterMap(jdbcUrl);

        if (jdbcMap == null) throw new RuntimeException("jdbc url is error: " + jdbcUrl);

        // todo 在框架中，需要赋予相应的 classloader，此处暂不做修改

        basicDataSource.setDriverClassName(driverClassName);

        basicDataSource.setUrl(jdbcMap.get("") + "?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8");
        basicDataSource.setUsername(jdbcMap.get("user"));
        basicDataSource.setPassword(jdbcMap.get("password"));

        String maxConnLifetimeMillis = jdbcMap.get("maxConnLifetimeMillis");
        // 链接最大生存时间
        basicDataSource.setMaxConnLifetimeMillis(
                maxConnLifetimeMillis != null ? Integer.valueOf(maxConnLifetimeMillis) : -1
        );

        return basicDataSource;
    }


    /**
     * 组装插入 SQL
     *
     * @param name      表名
     * @param fields    字段名数组
     */
    static String getInsertSql(String name, String[] fields) {
        return CharUtil.replacePlaceholders(
                "{}",
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
    static String getUpdateSql(String name, String[] fields) {
        return CharUtil.replacePlaceholders(
                "{}",
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
    static String getSelectSql(String name, String[] fields) {
        return CharUtil.replacePlaceholders(
                "{}",
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
    static String getCountSql(String name, String field) {
        return CharUtil.replacePlaceholders(
                "{}",
                "SELECT count({}) FROM {}",
                field,
                name
        );
    }


}
