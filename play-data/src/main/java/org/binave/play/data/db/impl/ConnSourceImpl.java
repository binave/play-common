package org.binave.play.data.db.impl;

import org.apache.commons.dbcp2.BasicDataSource;
import org.binave.common.api.Source;
import org.binave.play.data.args.DBConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接池实现
 *
 * @author by bin jin on 2017/6/12.
 * @since 1.8
 */
public class ConnSourceImpl implements Source<Connection> {

    private DBConfig dbConfig;

    public ConnSourceImpl(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    private BasicDataSource basicDataSource; // 连接池

    /**
     * 在使用时加载，提前加载会因为环境变量延迟加载而报错
     * @return 数据库链接
     */
    @Override
    public Connection create() {
        try {
            BasicDataSource dataSource = this.basicDataSource;
            if (dataSource == null) {
                dataSource = getBasicDataSource();
                this.basicDataSource = dataSource;
            }
            Connection connection = dataSource.getConnection();
            // 自动提交，有实物时，则不进行自动提交
            connection.setAutoCommit(true);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
