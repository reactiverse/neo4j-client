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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.neo4j.driver.Record;
import org.neo4j.driver.async.ResultCursor;

import static io.reactiverse.neo4j.Util.fromCompletionStage;
import static io.reactiverse.neo4j.Util.setHandler;

public class ResultCursorImpl implements io.reactiverse.neo4j.ResultCursor {

    private final ResultCursor cursor;
    private final Vertx vertx;

    public ResultCursorImpl(ResultCursor cursor, Vertx vertx) {
        this.cursor = cursor;
        this.vertx = vertx;
    }

    @Override
    public io.reactiverse.neo4j.ResultCursor one(Handler<AsyncResult<Record>> handler) {
        Future<Record> one = one();
        setHandler(one, handler);
        return this;
    }

    @Override
    public Future<Record> one() {
        return fromCompletionStage(cursor.nextAsync(), vertx.getOrCreateContext());
    }
}