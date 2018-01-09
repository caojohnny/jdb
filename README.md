JDB
===

``` shell
cd src/test/java/com/gmail/woodyc40/topics
javac Scenario
cd ../../../..
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n -javaagent:../../../topics-agent/target/topics-agent-1.0-SNAPSHOT.jar com.gmail.woodyc40.topics.Scenario
```