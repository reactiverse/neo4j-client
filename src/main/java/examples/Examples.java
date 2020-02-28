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

package examples;

import io.reactiverse.neo4j.Neo4jClient;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.docgen.Source;
import org.neo4j.driver.Query;
import org.neo4j.driver.Values;

import java.util.ArrayList;
import java.util.List;

@Source
public class Examples {

    public void sharedClient(Vertx vertx, JsonObject config) {
        Neo4jClient.createShared(vertx, config);
    }

    public void simpleFindExample(Neo4jClient neo4jClient) {

        neo4jClient.find("MATCH (you {name:'You'})-[:FRIEND]->(yourFriends) RETURN yourFriends")
                   .onSuccess(records -> System.out.println("Got " + records.size() + " friends"))
                   .onFailure(error -> System.out.println("Failure: " + error.getMessage()));
    }

    public void simpleFindOneExample(Neo4jClient neo4jClient) {

        neo4jClient.findOne("MATCH (p:Person {name:$name}) RETURN p", Values.parameters("name", "John"))
                .onSuccess(record -> System.out.println("Got my person"))
                .onFailure(error -> System.out.println("Failure: " + error.getMessage()));
    }

    public void streamingRecords(Neo4jClient neo4jClient) {

        neo4jClient.queryStream("MATCH (you {name:$name})-[:FRIEND]->(yourFriends) RETURN yourFriends", Values.parameters("name", "John"))
                .onSuccess(neo4jRecordStream -> {
                    neo4jRecordStream.handler(record -> System.out.println("Got a new record"));
                    neo4jRecordStream.endHandler(v -> System.out.println("All records streamed"));
                    neo4jRecordStream.exceptionHandler(error -> System.out.println("Failure: " + error.getMessage()));
                })
                .onFailure(error -> System.out.println("Failure: " + error.getMessage()));
    }

    public void executeQueriesInTransaction(Neo4jClient neo4jClient) {

        neo4jClient.begin().flatMap(tx ->
            tx.query("CREATE (you:Person {name:$name}) RETURN you", Values.parameters("name", "John"))
            .flatMap(ignore -> tx.query("CREATE (you:Person {name:$name}) RETURN you", Values.parameters("name", "Jane")))
            .flatMap(ignore -> tx.query("MATCH (p1:Person {name:$name1}), (p2:Person {name:$name2}) CREATE (p1)-[:FRIEND]->(p2)", Values.parameters("name1", "John", "name2", "Jane")))
            .flatMap(ignore -> tx.commit())
            .recover(error -> tx.rollback().onSuccess(v -> System.out.println("Transaction rollbacked because: " + error.getMessage())))
        )
        .onSuccess(v -> System.out.println("Transaction committed"))
        .onFailure(error -> System.out.println("Failure: " + error.getMessage()));
    }

    public void shouldWriteInBatch(Neo4jClient neo4jClient) {

        List<Query> queries = new ArrayList<Query>() {{
            add(new Query("CREATE (you:Person {name:$name}) RETURN you", Values.parameters("name", "John")));
            add(new Query("CREATE (you:Person {name:$name}) RETURN you", Values.parameters("name", "Jane")));
            add(new Query("MATCH (p1:Person {name:$name1}), (p2:Person {name:$name2}) CREATE (p1)-[:FRIEND]->(p2)", Values.parameters("name1", "John", "name2", "Jane")));
        }};

        neo4jClient.bulkWrite(queries)
            .onSuccess(ignore -> System.out.println("Transaction committed"))
            .onFailure(error -> System.out.println("Transaction rollbacked because: " + error.getMessage()));
    }
}
