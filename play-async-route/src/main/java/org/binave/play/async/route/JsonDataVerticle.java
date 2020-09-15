package org.binave.play.async.route;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.binave.common.util.TypeUtil;
import org.binave.play.config.JsonUtil;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;

/**
 *
 *
 * @author by binjinj on 2020/9/9 22:12.
 */
@Slf4j
abstract public class JsonDataVerticle<Pojo> extends SyncVerticle {

    private Class<?> type;

    protected JsonDataVerticle() {
        Class<?> subClass = this.getClass();
        while (subClass.getSuperclass() != JsonDataVerticle.class) {
            subClass = subClass.getSuperclass();
            if (subClass == null || subClass == Class.class) {
                throw new RuntimeException("class type error.");
            }
        }
        Type[] types = TypeUtil.getGenericTypes(subClass.getGenericSuperclass());
        if (types == null || types.length == 0)
            throw new RuntimeException(
                    String.format("not found generic class: %s", subClass.getName())
            );
        this.type = (Class<?>) types[0];
    }

    @Suspendable
    public void handler(RoutingContext ctx) {
        Object o;
        try {
            o = handler((Pojo) JsonUtil.toObject(ctx.getBodyAsString(), type));
        } catch (RuntimeException e) {
            HttpServerRequest request = ctx.request();
            SocketAddress address = request.remoteAddress();
            log.error("'{}:{}' -> '{}{}'", address.host(), address.port(), request.host(), request.uri());
            ctx.response().
                    putHeader("content-type", "text/plain").
                    setStatusCode(HttpURLConnection.HTTP_OK).
                    end("bad request");
            return;
        }

        String end = null;
        if (o instanceof String) {

        } else {

        }
        ctx.response().
                putHeader("content-type", "text/plain").
                setStatusCode(HttpURLConnection.HTTP_OK).
                end(end);
    }

    @Suspendable
    abstract public <T> T handler(Pojo pojo);

}
