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

    private long version;

    private boolean override;

    private Set<String> tokens;

}
