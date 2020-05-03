[![Build Status](https://travis-ci.org/reactiverse/neo4j-client.svg?branch=master)](https://travis-ci.org/github/reactiverse/neo4j-client)

# Neo4j client for Vert.x

The Vert.x Neo4j client is an extension for interacting with [Neo4j](https://neo4j.com/neo4j-graph-database/).

The client is built on top of official [Neo4j Java Driver](https://github.com/neo4j/neo4j-java-driver).

## Version compatibility matrix

| Project | Vert.x | Neo4j Java Driver |
| ------- | ------ | ----------------- |
|  0.2.0  | 3.8.5  |       4.0.0       |

At the moment, tests have been performed only on Neo4j Server 3.5.x.
Neo4j Server 4.0 should be supported soon.

## Javadoc

The [Javadoc](./javadoc/index.html).

## Install

Using maven:
```
<dependency>
    <groupId>io.reactiverse</groupId>
    <artifactId>neo4j-client</artifactId>
    <version>0.2.0</version>
</dependency>
```

Using Gradle:
```
implementation("io.reactiverse:neo4j-client:0.2.0")
```

## Sample usage

Initialize the client:

```java
Neo4jClientOptions options = new Neo4jClientOptions().setHost("localhost").setPort(7687);
Neo4jClient neo4jClient = Neo4jClient.createShared(vertx, options);
```

Here is a sample usage with Java API where we ask to retrieve a list of friends:

```java
neo4jClient.find("MATCH (you {name:'You'})-[:FRIEND]->(yourFriends) RETURN yourFriends", find -> {
    if (find.succeeded()) {
        List<Record> result = find.result();
        // handle result
    } else {
        Throwable error = find.cause();
        // handle error
    }
});
```

And here is the RxJava 2 API equivalent:

```java
neo4jClient
    .rxFind("MATCH (you {name:'You'})-[:FRIEND]->(yourFriends) RETURN yourFriends")
    .subscribe(result -> {
        // handle result
    }, error -> {
        // handle error
    });
```

## Legal

Originally developed by :

[![](https://olympe.ch/wp-content/uploads/2018/08/logo_olympe_white.png)](https://olympe.ch/)

    Copyright (c) 2018-2020 Olympe S.A.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
