package org.binave.play.data.args;

/**
 * 与类型一一对应的 sql
 *
 * @author by bin jin on 2017/6/3.
 * @since 1.8
 */
public interface TypeSql {

    /**
     * 获得插入 sql
     */
    String getInsertSql();

    /**
     * 获得更新 sql
     */
    String getUpdateSql(String whereCondition);

    /**
     * 获得
     */
    String getSelectSql(String whereCondition);

    String getCountSql(String whereCondition);
}
