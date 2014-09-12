---
tags: [bitcoin, property graph, graph databases, neo4j]
projects: [BitcoinVisualizer]
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
- one or more ledger files in json/ subfolder

Build
-----

Use this command to build a single executable JAR file that parses JSON-formatted blockchain ledger files and convert them into a database for neo4j.

```
mvn clean package 
```


Run
---

Use this command to run the executable JAR which will parse the JSON files contained in the json/ folder into a neo4j database:
```
java -jar target/btcviz-0.1.0.jar
```

Quickstart
----------

You can configure your AMI, build & run your service in one line by typing: 
```
sudo apt-get update && sudo apt-get install -y git maven && git clone https://github.com/gleim/btcviz && cd btcviz && mvn clean package && java -jar target/btcviz-0.1.0.jar
```


Result
------
The result of running the executable JAR file will be a new *btcvizneo4j.db* file located in btcviz/db/


AWS AMI configuration
---------------------

Launch a 64-bit Ubuntu AMI & run this command to add neo4j:

```
wget http://dist.neo4j.org/neo4j-community-2.0.1-unix.tar.gz && tar -xvzf neo4j-community-2.0.1-unix.tar.gz && mv neo4j-community-2.0.1 neo4j
```

Neo4j configuration
-------------------

To configure neo4j for use on the AMI, follow these steps:


Edit the following file:
```
sudo vi /etc/security/limits.conf
```

Add the following lines to allow neo to create files as needed:
```
*    soft    nofile    40000
*    hard    nofile    40000
```

Edit the following file:
```
vi neo4j/conf/neo4j-server.properties
```

Add the following line to allow external viewers to access:
```
org.neo4j.server.webserver.address=0.0.0.0
```

Modify the database.location to point to the database created with the executable JAR:
```
org.neo4j.server.database.location=/home/ubuntu/btcviz/db/btcvizneo4j.db
```

Prepare to run neo4j
--------------------

At this point, if you type:
```
ulimit -n
```
you will see the old Linux file maximum of 1024.


Reboot the AMI to pick up the changes before proceeding further.


At this point, if you type:
```
ulimit -n
```
you will see the new Linux file maximum of 40000, which is the minimum number of files needed to support neo4j operation.


Run neo4j with btcviz database
------------------------------

Start neo4j:
```
./neo4j/bin/neo4j start
```


Result
------

We have used an embedded Neo4j server to store some simple related entities from the Bitcoin ledger.  Now you can visualize the data in your web browser &/or run more advanced queries against the relations.

