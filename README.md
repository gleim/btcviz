---
tags: [spring-data, neo4j]
projects: [spring-data-neo4j]
---
:toc:
:icons: font
:source-highlighter: prettify
:project_id: gs-accessing-data-neo4j
This guide walks you through the process of using Spring Data to build an application with Neo4j.

== What you'll build

You'll use Neo4j's link:/understanding/NoSQL[NoSQL] graph-based data store to build an embedded Neo4j server, store entities and relationships, and develop queries.

== What you'll need

include::https://raw.github.com/spring-guides/getting-started-macros/master/prereq_editor_jdk_buildtools.adoc[]

include::https://raw.github.com/spring-guides/getting-started-macros/master/how_to_complete_this_guide.adoc[]


[[scratch]]
== Set up the project

include::https://raw.github.com/spring-guides/getting-started-macros/master/build_system_intro.adoc[]

include::https://raw.github.com/spring-guides/getting-started-macros/master/create_directory_structure_hello.adoc[]


include::https://raw.github.com/spring-guides/getting-started-macros/master/create_both_builds.adoc[]
`build.gradle`
// AsciiDoc source formatting doesn't support groovy, so using java instead
[source,java]
----
include::initial/build.gradle[]
----

include::https://raw.github.com/spring-guides/getting-started-macros/master/spring-boot-gradle-plugin.adoc[]

[[initial]]
== Define a simple entity
Neo4j captures entities and their relationships, with both aspects being of equal importance. Imagine you are modeling a system where you store a record for each person. But you also want to track a person's co-workers (`teammates` in this example). With Neo4j, you can capture all that with some simple annotations.

`src/main/java/hello/Person.java`
[source,java]
----
include::complete/src/main/java/hello/Person.java[]
----

Here you have a `Person` class that has only one attribute, the `name`. You have two constructors, an empty one as well as one for the `name`. To use Neo4j later on, you need the empty constructor. The name-based one is for convenience.

NOTE: In this guide, the typical getters and setters are omitted for brevity.

The `Person` class is annotated `@NodeEntity`. When Neo4j stores it, it results in the creation of a new node. This class also has an `id` marked `@GraphId`. Neo4j uses `@GraphId` internally to track the data.

The next important piece is the set of `teammates`. It is a simple `Set<Person>`, but marked up as `@RelatedTo`. This means that every member of this set is expected to also exist as a separate `Person` node. Note how the direction is set to `BOTH`. This means that when you generate a `TEAMMATE` relationship in one direction, it exists in the other direction as well. There is also a `@Fetch` annotation on this field as well. This causes the teammates to be eagerly retrieved. Otherwise you would have to use neo4jTemplate.fetch().

With the `worksWith()` method, you can easily link people together.

Finally, you have a convenient `toString()` method to print out the person's name and that person's co-workers.

== Create simple queries
Spring Data Neo4j is focused on storing data in Neo4j. But it inherits functionality from the Spring Data Commons project, including the ability to derive queries. Essentially, you don't have to learn the query language of Neo4j, but can simply write a handful of methods and the queries are written for you.

To see how this works, create an interface that queries `Person` nodes.

`src/main/java/hello/PersonRepository.java`
[source,java]
----
include::complete/src/main/java/hello/PersonRepository.java[]
----
    
`PersonRepository` extends the `GraphRepository` class and plugs in the type it operates on: `Person`. Out-of-the-box, this interface comes with many operations, including standard CRUD (create-read-update-delete) operations.

But you can define other queries as needed by simply declaring their method signature. In this case, you added `findByName`, which seeks nodes of type `Person`and finds the one that matches on `name`. You also have `findByTeammatesName`, which looks for a `Person` node, drills into each entry of the `teammates` field, and matches based on the teammate's `name`.

Let's wire this up and see what it looks like!

== Create an Application class
Create an Application class with all the components.

`src/main/java/hello/Application.java`
[source,java]
----
include::complete/src/main/java/hello/Application.java[]
----

In the configuration, you need to add the `@EnableNeo4jRepositories` annotation as well as extend the `Neo4jConfiguration` class to conveniently spin up needed components.

One piece that's missing is the graph database service bean. In this case, you are using the `EmbeddedGraphDatabase`, which creates and reuses a file-based data store at **accessingdataneo4j.db**.

NOTE: In a production environment, you would probably connect to a standalone, running Neo4j server instead.

You autowire an instance of `PersonRepository` that you defined earlier. Spring Data Neo4j will dynamically create a concrete class that implements that interface and will plug in the needed query code to meet the interface's obligations.

The `public static void main` uses Spring Boot's `SpringApplication.run()` to launch the application and invoke the `CommandLineRunner` that builds the relationships.

In this case, you create three local `Person` s, **Greg**, **Roy**, and **Craig**. Initially, they only exist in memory. It's also important to note that no one is a teammate of anyone (yet).

To store anything in Neo4j, you must start a transaction using the `graphDatabase`. In there, you will save each person. Then, you fetch each person, and link them together.

At first, you find Greg and indicate that he works with Roy and Craig, then persist him again. Remember, the teammate relationship was marked as `BOTH`, that is, bidirectional. That means that Roy and Craig will have been updated as well.

That's why when you need to update Roy, it's critical that you fetch that record from Neo4j first. You need the latest status on Roy's teammates before adding **Craig** to the list.

Why is there no code that fetches Craig and adds any relationships? Because you already have! **Greg** earlier tagged Craig as a teammate, and so did Roy. That means there is no need to update Craig's relationships again. You can see it as you iterate over each team member and print their information to the console.

Finally, check out that other query where you look backwards, answering the question "who works with whom?"


include::https://raw.github.com/spring-guides/getting-started-macros/master/build_an_executable_jar_subhead.adoc[]

include::https://raw.github.com/spring-guides/getting-started-macros/master/build_an_executable_jar_with_both.adoc[]

include::https://raw.github.com/spring-guides/getting-started-macros/master/run_the_application_with_both.adoc[]
    
You should see something like this (with other stuff like queries as well):
....
Before linking up with Neo4j...
Greg's teammates include

Roy's teammates include

Craig's teammates include

Lookup each person by name...
Greg's teammates include
	- Craig
	- Roy

Roy's teammates include
	- Craig
	- Greg

Craig's teammates include
	- Roy
	- Greg

Looking up who works with Greg...
Roy works with Greg.
Craig works with Greg.
....

You can see from the output that initially no one is connected by any relationship. Then after adding people in, they are tied together. Finally, you can see the handy query that looks up people based on teammate.

== Summary
Congratulations! You just set up an embedded Neo4j server, stored some simple, related entities, and developed some quick queries.

