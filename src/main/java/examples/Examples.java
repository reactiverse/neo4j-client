package examples;

import io.reactiverse.neo4j.Neo4jClient;
import io.reactiverse.neo4j.Neo4jRecordStream;
import io.reactiverse.neo4j.Neo4jTransaction;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.docgen.Source;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.neo4j.driver.summary.ResultSummary;

import java.util.ArrayList;
import java.util.List;

@Source
public class Examples {

    public void sharedClient(Vertx vertx, JsonObject config) {
        Neo4jClient.createShared(vertx, config);
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
                }).setHandler(done -> {
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
