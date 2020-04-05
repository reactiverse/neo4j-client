/*
 * Copyright (c) 2018-2020 Olympe S.A.
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

package io.reactiverse.neo4j.impl;

import io.vertx.core.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.async.ResultCursor;

public class ResultCursorImpl implements io.reactiverse.neo4j.ResultCursor {

    private final ResultCursor cursor;
    private final Vertx vertx;

    public ResultCursorImpl(ResultCursor cursor, Vertx vertx) {
        this.cursor = cursor;
        this.vertx = vertx;
    }

    @Override
    public io.reactiverse.neo4j.ResultCursor one(Handler<AsyncResult<Record>> handler) {
        Context context = vertx.getOrCreateContext();
        cursor.nextAsync()
                .thenAccept(record -> {
                    context.runOnContext(v -> handler.handle(Future.succeededFuture(record)));
                })
                .exceptionally(error -> {
                    context.runOnContext(v -> handler.handle(Future.failedFuture(error)));
                    return null;
                });
        return this;
    }
}
