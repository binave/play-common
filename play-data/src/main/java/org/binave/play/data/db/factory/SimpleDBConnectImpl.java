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

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.binave.common.api.Source;
import org.binave.common.api.SourceBy;
import org.binave.play.data.api.DBConnect;
import org.binave.play.data.api.DBTransact;
import org.binave.play.data.args.Dao;
import org.binave.play.data.args.TypeSql;

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
 * @see DBConnectFactory#createDBConnect(String, SourceBy)
 * @see DBConnectFactory#createDBConnect(Source, SourceBy)
 * @see DBConnectFactory#createDBTransact(String, SourceBy)
 * @see DBConnectFactory#createDBTransact(Source, SourceBy)
 *
 * @author bin jin
 * @since 1.8
 */
class SimpleDBConnectImpl implements DBConnect<Dao>, DBTransact<Dao> {

    private Source<Connection> connSource;
    private SourceBy<Class<? extends Dao>, TypeSql> sqlSource;

    private final static String LOCK = " FOR UPDATE";

    private long version;

    private Random random = new SecureRandom();

    // todo 每个 key 在 map 中存留的时间需要限制
    private Map<Long, Connection> stampMap = new ConcurrentHashMap<>();

    private QueryRunner queryRunner = new QueryRunner(); // jdbc

    SimpleDBConnectImpl(Source<Connection> connSource, SourceBy<Class<? extends Dao>, TypeSql> sqlSource) throws SQLException {
        this.connSource = connSource;
        this.sqlSource = sqlSource;
    }

    @Override
    public long lock() {
        long stamp;
        do {
            stamp = random.nextLong();
        } while (stampMap.containsKey(stamp));

        try (Connection connection = connSource.create()){
            connection.setAutoCommit(false);
            stampMap.put(stamp, connection);
        } catch (SQLException e) {
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

        String sql = sqlSource.create(params[0].getClass()).getInsertSql();

        try (Connection connection = connSource.create()) {
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

        String sql = sqlSource.create(params[0].getClass()).getInsertSql();
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
        try (Connection connection = connSource.create()) {
            return queryRunner.update(
                    connection,
                    sqlSource.create(param.getClass()).getUpdateSql("id = " + param.getId()),
                    param.getParams()
            );
        }
    }

    @Override
    public int update(long stamp, Dao param) throws SQLException {
        if (param == null) throw new IllegalArgumentException();
        return queryRunner.update(
                stampMap.get(stamp),
                sqlSource.create(param.getClass()).getUpdateSql("id = " + param.getId()),
                param.getParams()
        );
    }

    @Override
    public <T extends Dao> List<T> list(Class<T> clazz, String whereCondition) throws SQLException {
        try (Connection connection = connSource.create()) {
            return queryRunner.query(
                    connection,
                    sqlSource.create(clazz).getSelectSql(whereCondition),
                    new BeanListHandler<>(clazz)
            );
        }
    }

    @Override
    public <T extends Dao> List<T> list(long stamp, Class<T> clazz, String whereCondition) throws SQLException {
        return queryRunner.query(
                stampMap.get(stamp),
                sqlSource.create(clazz).getSelectSql(whereCondition) + LOCK,
                new BeanListHandler<>(clazz)
        );
    }

    @Override
    public <T extends Dao> T get(Class<T> clazz, String whereCondition) throws SQLException {
        try (Connection connection = connSource.create()) {
            return queryRunner.query(
                    connection,
                    sqlSource.create(clazz).getSelectSql(whereCondition),
                    new BeanHandler<>(clazz)
            );
        }
    }

    @Override
    public <T extends Dao> T get(long stamp, Class<T> clazz, String whereCondition) throws SQLException {
        return queryRunner.query(
                stampMap.get(stamp),
                sqlSource.create(clazz).getSelectSql(whereCondition) + LOCK,
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

}
