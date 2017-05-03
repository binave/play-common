package org.binave.play.data.args;

import lombok.*;

/**
 * 用于标识身份
 *
 * @author by bin jin on 2017/5/13.
 * @since 1.8
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Access extends Dao {

    private long id;

    private int pool;

    @Override
    public Object[] getParams() {
        // 不落地
        throw new UnsupportedOperationException("not support");
    }
}
