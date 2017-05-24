package org.binave.play.config.args;

import lombok.*;

import java.util.Set;

/**
 * 传递更新通知
 *
 * 注意：
 *      boolean 前不允许加 is
 *
 * @author by bin jin on 2017/5/10.
 * @since 1.8
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotice {

    /**
     * 版本号，用于确定关联配版本一致性问题
     */
    private long version;

    /**
     * 是否进行覆盖更新
     */
    private boolean override;

    /**
     * 更新目标
     */
    private String[] tokens;

}
