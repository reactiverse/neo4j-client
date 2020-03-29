/*
 * Copyright 2020 Olympe SA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactiverse.neo4j;

import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public final class Util {

    private Util() {}

    public static <T> void setHandler(Future<T> future, Handler<AsyncResult<T>> handler) {
        Objects.requireNonNull(future, "future must not be null");
        if (handler != null) {
            future.onComplete(handler);
        }
    }

    public static <T> Future<T> fromCompletionStage(CompletionStage<T> completionStage, Context context) {
        Promise<T> promise = ((ContextInternal) context).promise();
        completionStage.whenComplete((value, err) -> {
            if (err != null) {
                promise.fail(Optional.ofNullable(err.getCause()).orElse(err));
            } else {
                promise.complete(value);
            }
        });
        return promise.future();
    }
}
