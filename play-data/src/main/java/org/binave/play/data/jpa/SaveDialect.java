package org.binave.play.data.jpa;

import org.hibernate.dialect.MySQLDialect;

/**
 * {@link MySQLDialect}
 * @author by bin jin on 2019/9/17 16:36.
 */
public class SaveDialect extends MySQLDialect {

    @Override
    public String getTableTypeString() {
        return " charset=utf8mb4 ";
    }
}
