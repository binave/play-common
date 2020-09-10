package org.binave.play.data.db;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author by binjinj on 2020/9/7 3:26.
 */
public class BDUtils {

    /**
     * 通过数据库获得 DDl （建表 SQL）
     */
    public static String getDDL(DataSourceManager manager, String tableName) {
        String url = manager.getUrl();
        if (url.contains(":mysql:")) {
            try (Connection conn = manager.getDataSource().getConnection()) {
                String finalTableName = null;

                ResultSet resultSet = conn.createStatement().executeQuery("show tables");
                while (resultSet.next()) { // 先匹配到表名。部分数据库设置表名区分大小写。
                    String name = resultSet.getString(1); // 列名不固定
                    if (Objects.equals(name.toLowerCase(), tableName.toLowerCase())) {
                        finalTableName = name; // 原本的表名
                        break;
                    }
                }

                if (finalTableName == null) {
                    throw new IllegalArgumentException(String.format(
                            "table: '%s' not exist, can not find DDL.", tableName
                    ));
                }

                resultSet = conn.createStatement().executeQuery(String.format(
                        "show create table %s", finalTableName
                ));

                // [{Table=, Create Table=}]
                if (resultSet.next()) {
                    return String.valueOf(resultSet.getObject("Create Table"));
                }

                throw new IllegalArgumentException(String.format(
                        "DDL: %s not find.", tableName
                ));

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (url.contains(":oracle:")) {
            throw new NotImplementedException();
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 查看某个表是否在被操作
     */
    public static boolean tableOpen(DataSourceManager manager, String tableName) {
        String url = manager.getUrl();
        String databaseName;
        if (url.contains(":mysql:")) {
            // mysql
            Matcher matcher = Pattern.compile("(?<=/)[A-Za-z_][0-9A-Za-z_]+(?=\\?)").matcher(url); // 获得数据源的名称
            if (matcher.find()) {
                databaseName = matcher.group();
            } else {
                throw new IllegalArgumentException();
            }

            try (Connection conn = manager.getDataSource().getConnection()) {

                ResultSet resultSet = conn.createStatement().executeQuery(
                        // show open tables from ? where `Table` = ? and In_use > 0 语法错误
                        String.format("show open tables from %s where In_use > 0", databaseName)
                );
                // [{Database=, Table=, In_use=1, Name_locked=0}]
                while (resultSet.next()) {
                    if (Objects.equals(
                            String.valueOf(resultSet.getObject("Table")).toLowerCase(),
                            String.valueOf(tableName).toLowerCase()
                    )) {
                        return true;
                    }
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        } else if (url.contains(":oracle:")) {
            throw new NotImplementedException();
        } else {
            throw new IllegalArgumentException();
        }
    }

}
