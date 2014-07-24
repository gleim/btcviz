---
tags: [bitcoin, property graph, spring-data, graph databases, neo4j]
projects: [spring-data-neo4j, BitcoinVisualizer]
---

This repo translates Bitcoin blockchain data from JSON format into a simple Neo4j graph database (for data visualization and analysis).

Objective
---------

You'll use Neo4j's graph-based data store to build an embedded Neo4j server, store Bitcoin blockchain entities and relationships.

Requirements
------------

- Java 7
- Neo4J Community Edition 2.0.1
- Maven 3.0+
- one or more ledger files in json/ (ledger #470000 is provided to start)

Build
-----

You can build a single executable JAR file that contains all the necessary dependencies, classes, and resources. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.


mvn clean package 


Then you can run the JAR file:

java -jar target/btcviz-0.1.0.jar

The procedure above will create a runnable JAR. You can also opt to build a classic WAR file instead.


Run
---

You can run your service by typing: 

 mvn clean package && java -jar target/btcviz-0.1.0.jar.


Or alternatively: 

mvn spring-boot:run

This service will produce JSON representation of the blockchain, with each invidual block represented in a unique .JSON file


AWS AMI configuration
---------------------

Note: Amazon Linux AMI includes Java by default; Ubuntu AMI does not


Launch a 64-bit Amazon Linux AMI & run this command to add neo4j:

```
sudo yum update -y && wget http://dist.neo4j.org/neo4j-community-2.0.1-unix.tar.gz && tar -xvzf neo4j-community-2.0.1-unix.tar.gz && mv neo4j-community-2.0.1 neo4j
```

To configure neo4j for use on the AMI, follow these steps:


Edit the following file:
```
sudo vi /etc/security/limits.conf
```

Add the following lines to allow neo to create files as needed:
```
*    soft    nofiles    40000
*    soft    nofiles    40000
```

Edit the following file:
```
vi neo4j/conf/neo4j-server.properties
```

Add the following line to allow external viewers to access:
```
org.neo4j.server.webserver.address=0.0.0.0
```

Start neo4j:
```
./neo4j/bin/neo4j start
```


Conclusion
----------

We have used an embedded Neo4j server to store some simple related entities from the Bitcoin ledger.  Now you can visualize the data in your web browser &/or run more advanced queries against the relations.

An example of this may be found at http://metabitco.in

