package org.binave.play.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author by bin jin on 2020/9/4 20:51.
 */
public class LogUtils {

    /**
     * 通过包名设置日志级别
     * 可以进行刷新
     */
    public static void setLogLevelByPackageNames(Map<String, String> logLevelInfoMap) {
        if (logLevelInfoMap == null || logLevelInfoMap.isEmpty()) {
            return;
        }

        ILoggerFactory lf = LoggerFactory.getILoggerFactory();
        if (lf instanceof LoggerContext) {
            LoggerContext context = (LoggerContext) lf;
            for (Map.Entry<String, String> logLevel : logLevelInfoMap.entrySet()) {
                context.
                        getLogger(logLevel.getKey()).
                        setLevel(Level.valueOf(
                                logLevel.getValue()
                        ));
            }
        }
    }

}
