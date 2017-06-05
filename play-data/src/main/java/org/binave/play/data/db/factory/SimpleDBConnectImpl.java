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

package org.binave.play.data.db.factory;

import org.binave.play.data.api.DBConnect;
import org.binave.play.data.api.DBTransact;
import org.binave.play.data.args.Dao;
import org.binave.play.data.args.DBConfig;
import org.binave.play.data.args.SqlFactory;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库链接操作实现
 *
 * 使用 java 7 try (AutoCloseable) 新特性，无需写 close 方法。
 *
 * @see DBConnectFactory#createDBConnect(String, SqlFactory)
 * @see DBConnectFactory#createDBConnect(DBConfig, SqlFactory)
 * @see DBConnectFactory#createDBTransact(String, SqlFactory)
 * @see DBConnectFactory#createDBTransact(DBConfig, SqlFactory)
 *
 * @author bin jin
 * @since 1.8
 */
class SimpleDBConnectImpl implements DBConnect<Dao>, DBTransact<Dao> {

    private SqlFactory<Dao> sqlFactory;

    private final static String LOCK = " FOR UPDATE";

    /**
     * 数据库启动配置
     */
    private DBConfig dbConfig;

    private long version;

    private Random random = new SecureRandom();

    private Map<Long, Connection> stampMap = new ConcurrentHashMap<>();

    private QueryRunner queryRunner = new QueryRunner(); // jdbc

    SimpleDBConnectImpl(DBConfig dbConfig, SqlFactory<Dao> sqlFactory) throws SQLException {
        this.dbConfig = dbConfig;
        this.sqlFactory = sqlFactory;
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
    public int add(Dao... params) throws SQLException {
        if (params == null || params.length == 0)
            throw new IllegalArgumentException("args is error");

        String sql = sqlFactory.getInsertSql(params[0].getClass());

        try (Connection connection = getConnection()) {
            if (params.length == 1) {
                return queryRunner.update(
                        connection,
                        sql,
                        params[0].getParams()
                );
            } else {
                int c = 0;
                int[] l = queryRunner.batch(
                        connection,
                        sql,
                        getParams(params)
                );

                for (int i : l) {
                    if (i != 1) continue;
                    c++;
                }

                return c;
            }
        }
    }

    @Override
    public int add(long stamp, Dao... params) throws SQLException {
        if (params == null || params.length == 0)
            throw new IllegalArgumentException("args is error");

        String sql = sqlFactory.getInsertSql(params[0].getClass());
        if (params.length == 1) {
            return queryRunner.update(
                    stampMap.get(stamp),
                    sql,
                    params[0].getParams()
            );
        } else {
            int c = 0;
            int[] l = queryRunner.batch(
                    stampMap.get(stamp),
                    sql,
                    getParams(params)
            );

            for (int i : l) {
                if (i != 1) continue;
                c++;
            }

            return c;
        }
    }

    @Override
    public int update(Dao param) throws SQLException {
        if (param == null) throw new IllegalArgumentException();
        try (Connection connection = getConnection()) {
            return queryRunner.update(
                    connection,
                    sqlFactory.getUpdateSql("id = " + param.getId(), param.getClass()),
                    param.getParams()
            );
        }
    }

    @Override
    public int update(long stamp, Dao param) throws SQLException {
        if (param == null) throw new IllegalArgumentException();
        return queryRunner.update(
                stampMap.get(stamp),
                sqlFactory.getUpdateSql("id = " + param.getId(), param.getClass()),
                param.getParams()
        );
    }

    @Override
    public <T extends Dao> List<T> list(Class<T> clazz, String whereCondition) throws SQLException {
        try (Connection connection = getConnection()) {
            return queryRunner.query(
                    connection,
                    sqlFactory.getSelectSql(whereCondition, clazz),
                    new BeanListHandler<>(clazz)
            );
        }
    }

    @Override
    public <T extends Dao> List<T> list(long stamp, Class<T> clazz, String whereCondition) throws SQLException {
        return queryRunner.query(
                stampMap.get(stamp),
                sqlFactory.getSelectSql(whereCondition, clazz) + LOCK,
                new BeanListHandler<>(clazz)
        );
    }

    @Override
    public <T extends Dao> T get(Class<T> clazz, String whereCondition) throws SQLException {
        try (Connection connection = getConnection()) {
            return queryRunner.query(
                    connection,
                    sqlFactory.getSelectSql(whereCondition, clazz),
                    new BeanHandler<>(clazz)
            );
        }
    }

    @Override
    public <T extends Dao> T get(long stamp, Class<T> clazz, String whereCondition) throws SQLException {
        return queryRunner.query(
                stampMap.get(stamp),
                sqlFactory.getSelectSql(whereCondition, clazz) + LOCK,
                new BeanHandler<>(clazz)
        );
    }

    /**
     * 根据业务，返回批量插入的数组
     */
    private Object[][] getParams(Dao... params) {
        Object[][] parameters = new Object[params.length][];
        int i = 0;
        for (Dao param : params) {
            parameters[i++] = param.getParams();
        }
        return parameters;
    }

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
