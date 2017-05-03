package org.binave.play;

/**
 * 用于灰度支持
 *
 * @author by bin jin on 2017/5/13.
 * @since 1.8
 */
public interface Gray {

    /**
     * 将忽略包名。仅仅通过方法名称访问
     */
    long grayCode();

}
