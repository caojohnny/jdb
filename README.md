JDB
===

# Debugger

```shell
# Where JAVA_HOME points to the JDK
%JAVA_HOME%\bin\java.exe -cp topics-server-1.
0-SNAPSHOT.jar;%JAVA_HOME%\lib\tools.jar com.gmail.woodyc40.topics.Main
```

# Target VM

```shell
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n -javaagent:topics-agent-1.0-SNAPSHOT.jar ...
```
