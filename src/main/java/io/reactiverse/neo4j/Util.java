package io.reactiverse.neo4j;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.Optional;
import java.util.function.BiConsumer;

public final class Util {

    private Util() {}

    public static <T> BiConsumer<T, Throwable> wrapCallback(Context context, Handler<AsyncResult<T>> resultHandler) {
        return (result, error) -> {
            context.runOnContext(v -> {
                if (error != null) {
                    resultHandler.handle(Future.failedFuture(Optional.ofNullable(error.getCause()).orElse(error)));
                } else {
                    resultHandler.handle(Future.succeededFuture(result));
                }
            });
        };
    }
}
