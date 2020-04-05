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

import io.reactiverse.neo4j.options.Neo4jClientOptions;
import io.reactiverse.reactivex.neo4j.Neo4jClient;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.docgen.Source;
import io.vertx.reactivex.core.Vertx;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;

import java.util.List;

@Source
public class RxExamples {

    public void sharedClient(Vertx vertx, Neo4jClientOptions config) {
        Neo4jClient.createShared(vertx, config);
    }

    public void simpleFindExample(Neo4jClient neo4jClient) {

        Single<List<Record>> single = neo4jClient.rxFind("MATCH (you {name:'You'})-[:FRIEND]->(yourFriends) RETURN yourFriends");

        single.subscribe(result -> {
            System.out.println("Got " + result.size() + " friends");
        }, error -> {
            System.out.println("Failure: " + error.getMessage());
        });
    }

    public void simpleFindOneExample(Neo4jClient neo4jClient) {

        Single<Record> single = neo4jClient.rxFindOne("MATCH (p:Person {name:$name}) RETURN p", Values.parameters("name", "John"));

        single.subscribe(result -> {
            System.out.println("Got my person");
        }, error -> {
            System.out.println("Failure: " + error.getMessage());
        });
    }

    public void streamingRecords(Neo4jClient neo4jClient) {

        Completable completable = neo4jClient
                .rxQueryStream("MATCH (you {name:$name})-[:FRIEND]->(yourFriends) RETURN yourFriends", Values.parameters("name", "John"))
                .flatMapCompletable(stream -> stream.toFlowable().flatMapCompletable(record -> {
                    System.out.println("Got a new record");
                    return Completable.complete();
                }));

        completable.subscribe(
            () -> {
                System.out.println("All records streamed");
            },
            error -> {
                System.out.println("Failure: " + error.getMessage());
            }
        );
    }

    public void executeQueriesInTransaction(Neo4jClient neo4jClient) {

        Completable completable = neo4jClient.rxBegin()
                .flatMapCompletable(tx -> tx.rxQuery("CREATE (you:Person {name:$name}) RETURN you", Values.parameters("name", "John"))
                        .flatMap(ignore -> tx.rxQuery("CREATE (you:Person {name:$name}) RETURN you", Values.parameters("name", "Jane")))
                        .flatMap(ignore -> tx.rxQuery("MATCH (p1:Person {name:$name1}), (p2:Person {name:$name2}) CREATE (p1)-[:FRIEND]->(p2)", Values.parameters("name1", "John", "name2", "Jane")))
                        .flatMapCompletable(ignore -> tx.rxCommit())
                        .onErrorResumeNext(error -> tx.rxRollback().andThen(Completable.defer(() -> Completable.error(error)))));

        completable.subscribe(
            () -> {
                System.out.println("Transaction committed");
            },
            error -> {
                System.out.println("Transaction rollbacked because: " + error.getMessage());
            }
        );
    }
}
