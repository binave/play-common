package org.binave.play.data.args;

/**
 * 与类型一一对应的 sql
 *
 * @author by bin jin on 2017/6/3.
 * @since 1.8
 */
public abstract class TypeSql {

    /**
     * 获得插入 sql
     */
    public String getInsertSql() {
        throw new UnsupportedOperationException();
    }

    /**
     * 获得更新 sql
     */
    public String getUpdateSql(String whereCondition) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获得
     */
    public String getSelectSql(String whereCondition) {
        throw new UnsupportedOperationException();
    }

    public String getCountSql(String whereCondition) {
        throw new UnsupportedOperationException();
    }
}
