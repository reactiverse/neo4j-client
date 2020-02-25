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

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import org.neo4j.driver.Record;

/**
 * A {@link ReadStream} for {@link Record} consumption.
 *
 */
@VertxGen
public interface Neo4jRecordStream extends ReadStream<Record> {

    @Override
    Neo4jRecordStream exceptionHandler(Handler<Throwable> handler);

    @GenIgnore(GenIgnore.PERMITTED_TYPE)
    @Override
    Neo4jRecordStream handler(Handler<Record> handler);

    @Override
    Neo4jRecordStream pause();

    @Override
    Neo4jRecordStream resume();

    @Override
    Neo4jRecordStream endHandler(Handler<Void> handler);

    @Override
    Neo4jRecordStream fetch(long l);
}
