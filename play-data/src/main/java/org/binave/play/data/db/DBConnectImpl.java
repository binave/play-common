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

import org.binave.play.data.db.TypeSql;
import org.binave.play.data.args.DBConfig;
import org.binave.play.data.api.DBConnect;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.binave.play.data.args.Dao;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 数据库链接操作实现
 *
 * 使用 java 7 try (AutoCloseable) 新特性，无需写 close 方法。
 *
 * @author bin jin
 * @since 1.8
 */
class DBConnectImpl implements DBConnect<Dao> {

    private final static String LOCK = " FOR UPDATE";

    /**
     * 数据库启动配置
     */
    private DBConfig dbConfig;

    private long version;

    private Random random = new SecureRandom();

    private Map<Long, Connection> stampMap = new HashMap<>();

    private QueryRunner queryRunner = new QueryRunner(); // jdbc

    private static Map<Class, TypeSql> sqlCacheMap = new HashMap<>();

    /**
     * 获得类对应的数据库字段名
     */
    private static TypeSql getSqlType(Class<? extends Dao> type) {
        TypeSql cache = sqlCacheMap.get(type);
        if (cache == null) {
            cache = new TypeSql(type);
            sqlCacheMap.put(type, cache);
        }
        return cache;
    }

    DBConnectImpl(DBConfig dbConfig) throws SQLException {
        this.dbConfig = dbConfig;
    }

    @Override
    public long lock() {
        long stamp;
        do {
            stamp = random.nextLong();
        } while (stampMap.containsKey(stamp));

        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            stampMap.put(stamp, connection);
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException se) {
                    throw new RuntimeException(se);
                }
            }
            throw new RuntimeException(e);
        }
        return stamp;
    }

    @Override
    public void unlock(long stamp) {
        unlock(stamp, false);
    }

    @Override
    public void rollback(long stamp) {
        unlock(stamp, true);
    }

    private void unlock(long stamp, boolean rollback) {
        Connection conn = stampMap.get(stamp);
        if (conn != null) {
            stampMap.remove(stamp);
            try (Connection connection = conn) {
                if (rollback) {
                    connection.rollback();
                } else connection.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public void add(Dao param) throws SQLException {
        if (param == null || param.getId() == 0)
            throw new IllegalArgumentException("args is error");
        TypeSql tSql = getSqlType(param.getClass());
        try (Connection connection = getConnection()) {
            int l = queryRunner.update(
                    connection,
                    tSql.getInsertSql(),
                    param.getParams()
            );
            if (l != 1) throw new IllegalArgumentException();
        }
    }

    @Override
    public void add(long stamp, Dao param) throws SQLException {
        if (param == null || param.getId() == 0)
            throw new IllegalArgumentException("args is error");
        TypeSql tSql = getSqlType(param.getClass());
        int l = queryRunner.update(
                stampMap.get(stamp),
                tSql.getInsertSql(),
                param.getParams()
        );
        if (l != 1) throw new IllegalArgumentException();
    }

    @Override
    public int update(Dao param) throws SQLException {
        if (param == null) throw new IllegalArgumentException();
        TypeSql tSql = getSqlType(param.getClass());
        try (Connection connection = getConnection()) {
            return queryRunner.update(
                    connection,
                    tSql.getUpdateSql("id = " + param.getId()),
                    param.getParams()
            );
        }
    }

    @Override
    public int update(long stamp, Dao param) throws SQLException {
        if (param == null) throw new IllegalArgumentException();
        TypeSql tSql = getSqlType(param.getClass());
        return queryRunner.update(
                stampMap.get(stamp),
                tSql.getUpdateSql("id = " + param.getId()),
                param.getParams()
        );
    }

//    // 语句执行
//    private long update(String sql, Object... params) throws SQLException {
//        try (Connection connection = getConnection()) {
//            return (long) queryRunner.update(connection, sql, params);
//        }
//    }
//
//    private long update(long stamp, String sql, Object... params) throws SQLException {
//        return (long) queryRunner.update(
//                stampMap.get(stamp),
//                sql,
//                params
//        );
//    }

//    // 批量语句执行
//    private int[] batch(String sql, Object[][] params) throws SQLException {
//        try (Connection connection = getConnection()) {
//            return queryRunner.batch(connection, sql, params);
//        }
//    }
//
//    private int[] batch(long stamp, String sql, Object[][] params) throws SQLException {
//        return queryRunner.batch(
//                stampMap.get(stamp),
//                sql,
//                params
//        );
//    }

    @Override
    public <T extends Dao> List<T> list(Class<T> clazz, String whereCondition) throws SQLException {
        TypeSql tSql = getSqlType(clazz);
        try (Connection connection = getConnection()) {
            return queryRunner.query(
                    connection,
                    tSql.getSelectSql(whereCondition),
                    new BeanListHandler<>(clazz)
            );
        }
    }

    @Override
    public <T extends Dao> List<T> list(long stamp, Class<T> clazz, String whereCondition) throws SQLException {
        TypeSql tSql = getSqlType(clazz);
        return queryRunner.query(
                stampMap.get(stamp),
                tSql.getSelectSql(whereCondition) + LOCK,
                new BeanListHandler<>(clazz)
        );
    }

//    // 查询 list
//    private <T> List<T> list(Class<T> clazz, String sql, Object... params) throws SQLException {
//        try (Connection connection = getConnection()) {
//            return queryRunner.list(connection, sql, new BeanListHandler<>(clazz), params);
//        }
//    }
//
//    private <T> List<T> list(long stamp, Class<T> clazz, String sql, Object... params) throws SQLException {
//        if (!sql.toUpperCase().contains(LOCK)) sql += LOCK;
//        return queryRunner.list(
//                stampMap.get(stamp),
//                sql,
//                new BeanListHandler<>(clazz),
//                params
//        );
//    }

    @Override
    public <T extends Dao> T get(Class<T> clazz, String whereCondition) throws SQLException {
        TypeSql tSql = getSqlType(clazz);
        try (Connection connection = getConnection()) {
            return queryRunner.query(
                    connection,
                    tSql.getSelectSql(whereCondition),
                    new BeanHandler<>(clazz)
            );
        }
    }

    @Override
    public <T extends Dao> T get(long stamp, Class<T> clazz, String whereCondition) throws SQLException {
        TypeSql tSql = getSqlType(clazz);
        return queryRunner.query(
                stampMap.get(stamp),
                tSql.getSelectSql(whereCondition) + LOCK,
                new BeanHandler<>(clazz)
        );
    }


//    private <T> T get(Class<T> clazz, String sql, Object... params) throws SQLException {
//        try (Connection connection = getConnection()) {
//            return queryRunner.query(connection, sql, new BeanHandler<>(clazz), params);
//        }
//    }
//
//    private <T> T get(long stamp, Class<T> clazz, String sql, Object... params) throws SQLException {
//        if (!sql.toUpperCase().contains(LOCK)) sql += LOCK;
//        return queryRunner.query(
//                stampMap.get(stamp),
//                sql,
//                new BeanHandler<>(clazz),
//                params
//        );
//    }


    private BasicDataSource basicDataSource; // 连接池

    /**
     * 在使用时加载，提前加载会因为环境变量延迟加载而报错
     * @return 数据库链接
     */
    private Connection getConnection() throws SQLException {
        if (basicDataSource == null) basicDataSource = getBasicDataSource();
        Connection connection = basicDataSource.getConnection();
        connection.setAutoCommit(true); // 自动提交，有实物时，则不进行自动提交
        return connection;
    }

    /**
     * 从环境变量中获得数据库链接信息，创建连接池
     */
    private BasicDataSource getBasicDataSource() throws SQLException {
        BasicDataSource dataSource = new BasicDataSource();
        // todo 在框架中，需要赋予相应的 classloader，此处暂不做修改
//         dataSource.setDriverClassLoader();
        dataSource.setDriverClassName(dbConfig.getDriverClassName());
        dataSource.setUrl(dbConfig.getUrl());
        dataSource.setUsername(dbConfig.getUsername());
        dataSource.setPassword(dbConfig.getPassword());
        dataSource.setMaxConnLifetimeMillis(dbConfig.getMaxConnLifetimeMillis());
        return dataSource;
    }

}
