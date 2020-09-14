package org.binave.play.data.api;

import org.binave.common.api.Version;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author by bin jin on 2020/9/14 16:29.
 */
public interface DataConf extends Version {

    Map<String, String> getProperties();

    DataSource convertToDataSource();

    String getJdbcUrl();
}
