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

package examples;

import io.reactiverse.neo4j.Neo4jClient;
import io.reactiverse.neo4j.Neo4jRecordStream;
import io.reactiverse.neo4j.Neo4jTransaction;
import io.reactiverse.neo4j.options.Neo4jClientOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.neo4j.driver.summary.ResultSummary;

import java.util.ArrayList;
import java.util.List;

public class Examples {

    public void defaultSharedClient(Vertx vertx, Neo4jClientOptions config) {
        Neo4jClient.createShared(vertx, config);
    }

    public void customSharedClient(Vertx vertx, Neo4jClientOptions config, String dataSourceName) {
        Neo4jClient.createShared(vertx, config, dataSourceName);
    }

    public void nonSharedClient(Vertx vertx, Neo4jClientOptions config) {
        Neo4jClient.createNonShared(vertx, config);
    }

    public void simpleCreateNodesAndRelationship(Neo4jClient neo4jClient) {

        neo4jClient.execute("CREATE (you:Person {name:$name1})-[:FRIEND]->(him:Person {name:$name2})", Values.parameters("name1", "John", "name2", "Jack"), ar -> {
            if (ar.succeeded()) {
                ResultSummary result = ar.result();
                System.out.println("Got " + result.counters().nodesCreated() + " new nodes created");
                System.out.println("Got " + result.counters().relationshipsCreated() + " new relationships created");
            } else {
                Throwable error = ar.cause();
                System.out.println("Failure: " + error.getMessage());
            }
        });
    }

    public void simpleDelete(Neo4jClient neo4jClient) {

        neo4jClient.delete("MATCH (you:Person {name:'You'}) DELETE you RETURN you", Values.parameters("name", "John"), ar -> {
            if (ar.succeeded()) {
                List<Record> results = ar.result();
                System.out.println("Got deleted records");
            } else {
                Throwable error = ar.cause();
                System.out.println("Failure: " + error.getMessage());
            }
        });
    }

    public void simpleFindExample(Neo4jClient neo4jClient) {

        neo4jClient.find("MATCH (you {name:'You'})-[:FRIEND]->(yourFriends) RETURN yourFriends", find -> {
            if (find.succeeded()) {
                List<Record> result = find.result();
                System.out.println("Got " + result.size() + " friends");
            } else {
                Throwable error = find.cause();
                System.out.println("Failure: " + error.getMessage());
            }
        });
    }

    public void simpleFindOneExample(Neo4jClient neo4jClient) {

        neo4jClient.findOne("MATCH (p:Person {name:$name}) RETURN p", Values.parameters("name", "John"), findOne -> {
            if (findOne.succeeded()) {
                Record result = findOne.result();
                System.out.println("Got my person");
            } else {
                Throwable error = findOne.cause();
                System.out.println("Failure: " + error.getMessage());
            }
        });
    }

    public void streamingRecords(Neo4jClient neo4jClient) {

        neo4jClient.queryStream("MATCH (you {name:$name})-[:FRIEND]->(yourFriends) RETURN yourFriends", Values.parameters("name", "John"), queryStream -> {
            if (queryStream.succeeded()) {
                Neo4jRecordStream neo4jRecordStream = queryStream.result();

                neo4jRecordStream.handler(record -> System.out.println("Got a new record"));

                neo4jRecordStream.endHandler(v -> System.out.println("All records streamed"));

                neo4jRecordStream.exceptionHandler(error -> System.out.println("Failure: " + error.getMessage()));
            } else {
                Throwable error = queryStream.cause();
                System.out.println("Failure: " + error.getMessage());
            }
        });
    }

    public void executeQueriesInTransaction(Neo4jClient neo4jClient) {

        neo4jClient.begin(beginTx -> {
            if (beginTx.succeeded()) {
                Neo4jTransaction tx = beginTx.result();
                Promise<ResultSummary> createJohnPromise = Promise.promise();
                tx.query("CREATE (you:Person {name:$name}) RETURN you", Values.parameters("name", "John"), createJohnPromise);
                createJohnPromise.future().compose(ignore -> {
                    Promise<ResultSummary> createJanePromise = Promise.promise();
                    tx.query("CREATE (you:Person {name:$name}) RETURN you", Values.parameters("name", "Jane"), createJanePromise);
                    return createJanePromise.future();
                }).compose(ignore -> {
                    Promise<ResultSummary> createRelationshipPromise = Promise.promise();
                    tx.query("MATCH (p1:Person {name:$name1}), (p2:Person {name:$name2}) CREATE (p1)-[:FRIEND]->(p2)", Values.parameters("name1", "John", "name2", "Jane"), createRelationshipPromise);
                    return createRelationshipPromise.future();
                })
                .compose(ignore -> {
                    Promise<Void> commitPromise = Promise.promise();
                    tx.commit(commitPromise);
                    return commitPromise.future();
                }).onComplete(done -> {
                    if (done.succeeded()) {
                        System.out.println("Transaction committed");
                    } else {
                        Throwable error = done.cause();
                        Promise<Void> rollbackPromise = Promise.promise();
                        tx.rollback(rollbackPromise);
                        System.out.println("Transaction rollbacked because: " + error.getMessage());
                    }
                });
            } else {
                Throwable error = beginTx.cause();
                System.out.println("Failure: " + error.getMessage());
            }
        });
    }

    public void shouldWriteInBatch(Neo4jClient neo4jClient) {

        List<Query> queries = new ArrayList<Query>() {{
            add(new Query("CREATE (you:Person {name:$name}) RETURN you", Values.parameters("name", "John")));
            add(new Query("CREATE (you:Person {name:$name}) RETURN you", Values.parameters("name", "Jane")));
            add(new Query("MATCH (p1:Person {name:$name1}), (p2:Person {name:$name2}) CREATE (p1)-[:FRIEND]->(p2)", Values.parameters("name1", "John", "name2", "Jane")));
        }};

        neo4jClient.bulkWrite(queries, bulkWrite -> {
            if (bulkWrite.succeeded()) {
                System.out.println("Transaction committed");
            } else {
                Throwable error = bulkWrite.cause();
                System.out.println("Transaction rollbacked because: " + error.getMessage());
            }
        });
    }
}
