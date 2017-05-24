package org.binave.play;

/**
 * 源
 * 类似工厂方法
 *
 * @author by bin jin on 2017/6/3.
 * @since 1.8
 */
public interface Source<T> {

    T create();

}
