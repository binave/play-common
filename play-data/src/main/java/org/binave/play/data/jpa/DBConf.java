package org.binave.play.data.jpa;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.binave.common.api.Version;
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
public class DBConf implements Version {

    private String name;                    // 并非数据库用户名
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private String dialect;                 // JPA 参数
    private boolean write;
    private String description;
    private int fetchSize;
    private double version;
    private Map<String, String> sqlDict;    // sql
    private Map<String, String> properties;
    private String[] packages;              // JPA 扫描包名

    public DataSource convertToDataSource() {
        throw new NotImplementedException();
    }

    public Map<String, ?> convertJPAProperties() {
        Map<String, String> conf = new HashMap<>();
        conf.put("javax.persistence.jdbc.driver", this.getDriverClassName());
        conf.put("javax.persistence.jdbc.url", this.getJdbcUrl());
        conf.put("javax.persistence.jdbc.user", this.getUsername());
        conf.put("javax.persistence.jdbc.password", this.getPassword());
        conf.put("hibernate.dialect", this.getDialect());
        conf.put("hibernate.hbm2ddl.auto", "update");
        if (this.getProperties() != null) {
            conf.putAll(this.getProperties());
        }
        return conf;
    }


}
