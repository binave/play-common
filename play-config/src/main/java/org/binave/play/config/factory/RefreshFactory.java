package org.binave.play.config.factory;

import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import org.binave.play.Source;
import org.binave.play.config.args.ConfigEditor;
import org.binave.play.config.util.Refresh;

import java.util.Map;

/**
 * 获得刷新用的配置
 *
 * @author by bin jin on 2017/6/3.
 * @since 1.8
 */
public class RefreshFactory {

    // Map
    public static Refresh createConfMap(Source<Map<Integer, ConfigEditor>> source) {
        return new ConfMapPoolImpl(source);
    }

    // Table
    public static Refresh createConfTable(Source<Table<Integer, Integer, ConfigEditor>> source) {
        return new ConfTablePoolImpl(source);
    }

    // MultiMap
    public static Refresh createConfMulti(Source<Multimap<Integer, ConfigEditor>> source) {
        return new ConfMultiPoolImpl(source);
    }

}
