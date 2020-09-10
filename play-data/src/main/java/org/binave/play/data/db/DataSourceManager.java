package org.binave.play.data.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.binave.common.api.Version;

import javax.sql.DataSource;

/**
 * @author by bin jin on 2019/9/29 14:01.
 */
@AllArgsConstructor
@Getter
@ToString
public class DataSourceManager implements Version {

    private String name;
    private double version;
    private String url;
    private DataSource dataSource;
    private int fetchSize;
    private boolean write;

}
