/*
 * Copyright 2019 Olympe SA.
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

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.NoSuchRecordException;
import org.neo4j.harness.junit.Neo4jRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.assertj.core.util.Lists.newArrayList;
import static org.neo4j.driver.Values.parameters;

@RunWith(VertxUnitRunner.class)
public class Neo4jClientIT {

    /**
     * System property to set a log manager for JUL
     */
    private static final  String JUL_LOGGING_MANAGER_PROPERTY = "java.util.logging.manager";

    /**
     * Log4j2 log manager for JUL
     */
    private static final String LOG4J2_JUL_LOG_MANAGER = "org.apache.logging.log4j.jul.LogManager";

    /**
     * The Vert.x instance
     */
    Vertx vertx;

    /**
     * The database configuration to be passed to Vert.x verticles
     */
    JsonObject dbConfig;

    /**
     * The Neo4j client
     */
    Neo4jClient neo4jClient;

    @Rule
    public Neo4jRule neo4j = new Neo4jRule();

    @BeforeClass
    public static void prepareAll() {
        System.setProperty(JUL_LOGGING_MANAGER_PROPERTY, LOG4J2_JUL_LOG_MANAGER);
    }

    @Before
    public void onSetUp() {
        vertx = Vertx.vertx();
        dbConfig = new JsonObject().put("url", neo4j.boltURI().toString());
        neo4jClient = Neo4jClient.createShared(vertx, new JsonObject().put("url", neo4j.boltURI().toString()));
    }

    @After
    public void onTearDown(TestContext context) {
        neo4jClient.close();
        vertx.close(context.asyncAssertSuccess());
    }

    @Test public void should_save(TestContext testContext) {
        Async async = testContext.async();
        neo4jClient.execute(CREATE_PERSON_QUERY).setHandler(savedPerson -> {
            if (savedPerson.failed()) {
                testContext.fail(savedPerson.cause());
            } else {
                testContext.assertEquals(savedPerson.result().counters().nodesCreated(), 1);
                async.complete();
            }
        });
    }

    @Test public void should_save_with_param(TestContext testContext) {
        Async async = testContext.async();
        neo4jClient.execute(CREATE_PERSON_QUERY_WITH_PARAM, parameters("name", "You")).setHandler(savedPerson -> {
            if (savedPerson.failed()) {
                testContext.fail(savedPerson.cause());
            } else {
                testContext.assertEquals(savedPerson.result().counters().nodesCreated(), 1);
                async.complete();
            }
        });
    }

    @Test public void should_findOne(TestContext testContext) {
        Async async = testContext.async();
        neo4jClient.execute(CREATE_PERSON_QUERY)
        .compose(savedPerson -> neo4jClient.findOne(FIND_PERSON_QUERY))
        .setHandler(foundPerson -> {
            if (foundPerson.failed()) {
                testContext.fail(foundPerson.cause());
            } else {
                testContext.assertEquals(foundPerson.result().values().get(0).asNode().get("name").asString(), "You");
                async.complete();
            }
        });
    }

    @Test public void should_findOne_with_param(TestContext testContext) {
        Async async = testContext.async();
        neo4jClient.execute(CREATE_PERSON_QUERY)
        .compose(savedPerson -> neo4jClient.findOne(FIND_PERSON_QUERY_WITH_PARAM, parameters("name", "You")))
        .setHandler(foundPerson -> {
            if (foundPerson.failed()) {
                testContext.fail(foundPerson.cause());
            } else {
                testContext.assertEquals(foundPerson.result().values().get(0).asNode().get("name").asString(), "You");
                async.complete();
            }
        });
    }

    @Test public void should_find(TestContext testContext) {
        Async async = testContext.async();
        neo4jClient.execute(CREATE_PERSON_QUERY)
        .compose(savedPerson -> neo4jClient.execute(CREATE_FRIENDS_QUERY))
        .compose(savedFriends -> neo4jClient.find(FIND_FRIENDS_QUERY))
        .setHandler(foundFriends -> {
            if (foundFriends.failed()) {
                testContext.fail(foundFriends.cause());
            } else {
                testContext.assertEquals(foundFriends.result().size(), 5);
                async.complete();
            }
        });
    }

    @Test public void should_find_with_param(TestContext testContext) {
        Async async = testContext.async();
        neo4jClient.execute(CREATE_PERSON_QUERY)
        .compose(savedPerson -> neo4jClient.execute(CREATE_FRIENDS_QUERY))
        .compose(savedFriends -> neo4jClient.find(FIND_FRIENDS_QUERY_WITH_PARAM, parameters("name", "You")))
        .setHandler(foundFriends -> {
            if (foundFriends.failed()) {
                testContext.fail(foundFriends.cause());
            } else {
                testContext.assertEquals(foundFriends.result().size(), 5);
                async.complete();
            }
        });
    }

    @Test public void should_delete(TestContext testContext) {
        Async async = testContext.async();
        neo4jClient.execute(CREATE_PERSON_QUERY)
        .compose(savedPerson -> neo4jClient.findOne(FIND_PERSON_QUERY))
        .compose(foundPerson -> neo4jClient.execute(DELETE_PERSON_QUERY))
        .setHandler(deletedPerson -> {
            if (deletedPerson.failed()) {
                testContext.fail(deletedPerson.cause());
            } else {
                testContext.assertEquals(deletedPerson.result().counters().nodesDeleted(), 1);
                async.complete();
            }
        });
    }

    @Test public void should_delete_with_param(TestContext testContext) {
        Async async = testContext.async();
        neo4jClient.execute(CREATE_PERSON_QUERY)
        .compose(savedPerson -> neo4jClient.findOne(FIND_PERSON_QUERY))
        .compose(foundPerson -> neo4jClient.execute(DELETE_PERSON_QUERY_WITH_PARAM, parameters("name", "You")))
        .setHandler(deletedPerson -> {
            if (deletedPerson.failed()) {
                testContext.fail(deletedPerson.cause());
            } else {
                testContext.assertEquals(deletedPerson.result().counters().nodesDeleted(), 1);
                async.complete();
            }
        });
    }

    @Test public void should_check_counter_after_bulk_write(TestContext testContext) {
        Async async = testContext.async();
        List<Query> queries = new ArrayList<>(3);
        queries.add(new Query("CREATE (:Company {name: $name})", parameters("name", "Wayne Enterprises")));
        queries.add(new Query("CREATE (:Person {name: $name})", parameters("name", "Alice")));
        queries.add(new Query("MATCH (person:Person {name: $employee}) MATCH (company:Company {name: $company}) CREATE (person)-[:WORKS_FOR]->(company)", parameters("employee", "Alice", "company", "Wayne Enterprises")));
        neo4jClient.bulkWrite(queries).setHandler(foundNodes -> {
            if (foundNodes.failed()) {
                testContext.fail(foundNodes.cause());
            } else {
                testContext.assertEquals(foundNodes.result().nodesCreated(), 2);
                testContext.assertEquals(foundNodes.result().relationshipsCreated(), 1);
                async.complete();
            }
        });
    }

    @Test public void should_check_transaction_has_been_committed_after_bulk_write(TestContext testContext) {
        Async async = testContext.async();
        List<Query> queries = new ArrayList<>(3);
        queries.add(new Query("CREATE (:Company {name: $name})", parameters("name", "Wayne Enterprises")));
        queries.add(new Query("CREATE (:Person {name: $name})", parameters("name", "Alice")));
        queries.add(new Query("MATCH (person:Person {name: $employee}) MATCH (company:Company {name: $company}) CREATE (person)-[:WORKS_FOR]->(company)", parameters("employee", "Alice", "company", "Wayne Enterprises")));
        neo4jClient.bulkWrite(queries)
        .compose(bulkDone -> neo4jClient.find("MATCH (person:Person)-[:WORKS_FOR]->(company:Company) RETURN person, company"))
        .setHandler(foundRecords -> {
            if (foundRecords.failed()) {
                testContext.fail(foundRecords.cause());
            } else {
                testContext.assertTrue(foundRecords.result().get(0).values().stream().map(value -> value.get("name").asString()).collect(Collectors.toList()).containsAll(newArrayList("Alice", "Wayne Enterprises")));
                async.complete();
            }
        });
    }

    @Test public void should_begin_transaction(TestContext testContext) {
        Async async = testContext.async();
        neo4jClient.begin().compose(tx -> {
            return tx.query("CREATE (:Company {name: $name})", parameters("name", "Wayne Enterprises"))
            .compose(resultSummary -> tx.query("CREATE (:Person {name: $name})", parameters("name", "Alice")))
            .compose(resultSummary -> tx.query("MATCH (person:Person {name: $employee}) MATCH (company:Company {name: $company}) CREATE (person)-[:WORKS_FOR]->(company)", parameters("employee", "Alice", "company", "Wayne Enterprises")))
            .compose(end -> tx.commit())
            .compose(committed -> neo4jClient.find("MATCH (person:Person)-[:WORKS_FOR]->(company:Company) RETURN person, company"));
        }).setHandler(foundRecords -> {
            if (foundRecords.failed()) {
                testContext.fail(foundRecords.cause());
            } else {
                testContext.assertTrue(foundRecords.result().get(0).values().stream().map(value -> value.get("name").asString()).collect(Collectors.toList()).containsAll(newArrayList("Alice", "Wayne Enterprises")));
                async.complete();
            }
        });
    }

    @Test public void should_check_transaction_is_not_committed(TestContext testContext) {
        Async async = testContext.async(1);
         neo4jClient.begin().compose(tx -> {
            return tx.query("CREATE (:Company {name: $name})", parameters("name", "Wayne Enterprises"))
            .compose(resultSummary -> tx.query("CREATE (:Person name: $name})", parameters("name", "Alice")))
            .compose(resultSummary -> tx.commit())
            .onFailure(error -> tx.rollback());
        }).setHandler(commitTransaction -> {
            if (commitTransaction.succeeded()) {
                testContext.fail("Transaction Commit should have failed");
            } else {
                testContext.assertTrue(commitTransaction.cause() instanceof ClientException);
                neo4jClient.findOne("MATCH (you:Company {name:$name}) RETURN you", parameters("name", "Wayne Enterprises")).setHandler(record -> {
                    if (record.failed()) {
                        testContext.assertTrue(record.cause().getCause() instanceof NoSuchRecordException);
                        async.complete();
                    } else {
                        testContext.fail("Record should not have been retrieved");
                    }
                });
            }
        });
    }

    @Test public void should_check_all_nodes_are_streamed(TestContext testContext) {
        Async async = testContext.async();
        neo4jClient
            .execute(CREATE_PERSON_QUERY)
            .compose(savedPerson -> neo4jClient.execute(CREATE_FRIENDS_QUERY))
            .setHandler(ignore -> {
                neo4jClient.queryStream(FIND_FRIENDS_QUERY_WITH_PARAM, parameters("name", "You"), testContext.asyncAssertSuccess(stream -> {
                    List<Record> items = Collections.synchronizedList(new ArrayList<>());
                    AtomicInteger idx = new AtomicInteger();
                    long pause = 500;
                    long start = System.nanoTime();
                    stream.endHandler(end -> {
                        long duration = NANOSECONDS.toMillis(System.nanoTime() - start);
                        testContext.assertEquals(items.size(),5);
                        testContext.assertTrue(duration >= 2 * pause);
                        async.complete();
                    })
                    .exceptionHandler(testContext::fail)
                    .handler(item -> {
                        testContext.assertTrue(item != null);
                        items.add(item);
                        int j = idx.getAndIncrement();
                        if (j == 1 || j == 3) {
                            stream.pause();
                            int emitted = items.size();
                            vertx.setTimer(pause, tid -> {
                                testContext.assertTrue(emitted == items.size());
                                stream.resume();
                            });
                        }
                    });
                }));
        });
    }

    // queries

    private static final String CREATE_PERSON_QUERY = "CREATE (you:Person {name:'You'}) RETURN you";

    private static final String CREATE_PERSON_QUERY_WITH_PARAM = "CREATE (you:Person {name:$name}) RETURN you";

    private static final String FIND_PERSON_QUERY = "MATCH (you:Person {name:'You'}) RETURN you";

    private static final String FIND_PERSON_QUERY_WITH_PARAM = "MATCH (you:Person {name:$name}) RETURN you";

    private static final String CREATE_FRIENDS_QUERY =
            "MATCH (you:Person {name:'You'})\n" +
                    "FOREACH (name in ['Johan', 'Rajesh', 'Anna', 'Julia', 'Andrew'] |\n" +
                    "  CREATE (you)-[:FRIEND]->(:Person {name:name}))";

    private static final String FIND_FRIENDS_QUERY = "MATCH (you {name:'You'})-[:FRIEND]->(yourFriends) RETURN yourFriends";

    private static final String FIND_FRIENDS_QUERY_WITH_PARAM = "MATCH (you {name:$name})-[:FRIEND]->(yourFriends) RETURN yourFriends";

    private static final String DELETE_PERSON_QUERY = "MATCH (you:Person {name:'You'}) DELETE you";

    private static final String DELETE_PERSON_QUERY_WITH_PARAM = "MATCH (you:Person {name:$name}) DELETE you";
}
