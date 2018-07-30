JDB
===

Advanced CS Projects class project.

JDB is a Java for Java programs similiar in function to C's GDB. I have implemented a lot fewer features, as I felt that it was complete halfway through the year. I didn't end up following up on a lot of "nice-to-haves" in the planning document, which can be found [here](https://github.com/AgentTroll/jdb/blob/master/jdb.md).

I gave a presentation to the staff about my project, the slides and annotated transcript can be found [on my blog](https://agenttroll.github.io/blog/2018/05/11/jdb-but-not-that-one.html).

# Debugger

```shell
# Where JAVA_HOME points to the JDK
%JAVA_HOME%\bin\java.exe -cp topics-server-1.0-SNAPSHOT.jar;%JAVA_HOME%\lib\tools.jar com.gmail.woodyc40.topics.Main
```

# Target VM

```shell
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n -javaagent:topics-agent-1.0-SNAPSHOT.jar ...
```
# Demo on `Scenario.java`

```
(jdb) ba sce:17
Breakpoint after com.gmail.woodyc40.topics.Scenario.main:17

Code sample:

>        System.out.println("ok");
        System.out.println("let's try again");

Hit breakpoint Scenario.java:17
Code context:
        Object o = new Object();
        Integer.valueOf(o.hashCode()).hashCode();

>        System.out.println("ok");
        System.out.println("let's try again");

        changeStack();
(jdb) returns
java.lang.Integer.<init>(int) = <void value>
java.lang.Integer.valueOf(int) = instance of java.lang.Integer(id=828)
java.lang.Integer.hashCode(int) = 875827115
java.lang.Integer.hashCode() = 875827115

(jdb) 
```
