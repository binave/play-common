package org.binave.play.data.jpa;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.binave.play.data.api.DataConf;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库配置类，使用 yaml 反序列化
 *
 * @author by bin jin on 2019/08/14 16:52.
 */
@Getter
@Setter
@ToString
public class DBConf implements DataConf {

    private String name;                    // 并非数据库用户名
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private boolean write;
    private String description;
    private int fetchSize;
    private double version;
    private Map<String, String> sqlDict;    // sql
    private Map<String, String> properties;

    public Map<String, String> getProperties() {
        Map<String, String> setting = new HashMap<>();
        setting.put("hibernate.hbm2ddl.auto", "update");
        setting.put("hibernate.show_sql", "false");
//        setting.put("hibernate.dialect", "org.binave.play.data.jpa.SaveDialect");
        if (properties != null) {
            setting.putAll(this.properties);
        }
        this.properties = setting;
        return this.properties;
    }

    public DataSource convertToDataSource() {
//        HikariDataSource ds = new HikariDataSource();
//        ds.setJdbcUrl(this.getJdbcUrl());
//        ds.setUsername(this.getUsername());
//        ds.setPassword(this.getPassword());
//        ds.setDriverClassName(this.getDriverClassName());
        throw new NotImplementedException();
    }

    @Override
    public void check() throws RuntimeException {
        if (jdbcUrl == null || jdbcUrl.length() < 5 ||
                username == null || password == null ||
                driverClassName == null) {
            throw new IllegalArgumentException(String.format("%s is empty",
                    jdbcUrl == null || jdbcUrl.length() < 5 ? "url" : (
                            username == null ? "username" : (
                                    password == null ? "password" : "driverClassName"
                            )
                    )
            ));
        }
    }
}
