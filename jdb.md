# JDB - Java Debugger

**Summary**: Create a GDB-like debugging program targeted to developers working with Java code in order to ease the process of decoupling the IDE with the running program

**Limitations of similar programs**:

  - **GDB**: Targeted towards `C` programs; incompatible with Java
    * However, forms a good starting point in terms of interface, design, "front-end"
  - **IntelliJ IDEA Debugger**: Must be run within IDE; sometimes not desireable/possible; clunky user interface; weird keybinds; most people don't even use IntelliJ IDEA
    * IntelliJ's front-end is very well designed and its ability to have Watches and filters is very intuitive and useful to developers
  - **Eclipse debugger**: I don't use eclipse, don't know how to use its debugger. Icons are slightly confusing, its use to the developer's advantage is not outlined due to complexity (?).
    * Eclipse is, however, a widely used program and perhaps a front-end could be developed in the form of a plugin that interfaces with a headless instance of Eclipse if needed, or perhaps even integrated with the IDE itself

## Deployment

The end result will probably be deployed in the form of a `.jar` file to end users. While it would be cool to have a `jdb` command, it may be difficult to do this on Windows due to administrative permissions in the `PATH` envar. This should probably be trivial on \*nix systems thanks to having files such as `.bashrc`, and `.profile`, but users of those platforms are few and far in between.

The presentation will probably consist of going through the front end and running through the different scenarios in order to show the capabilities that low-level debugging offers. The most important element at the end of the day is demonstrating its use in a typical developer workflow and showing how to troubleshoot common problems more productively.

## Target audience

The "end users" of this will most likely be proficient novice developers who might use `jdb` as a stepping stone to more comprehensive and powerful tools such as the built-in IDE debuggers.

Although the intended target would be novice developers, `jdb` may be more suited and attractive to more advanced end users hailing from `C` languages` and \*nix systems in the sense that its focus on CLI and "low-level" debugging will be more familiar and logical for those particular users.

## Goals

- Create a GDB-like CLI front end capable of receiving instructions and outputting debug information such as stack variables and program state
- Implement ViM-like keybindings (e.g. hjkl movement keys) for more intuitive and productive debugging
- Allow more comprehensive and intuitive breakpoints which occur *in-between* lines rather than on the line itself (as would be familiar for those experienced with IDE debuggers already) that will include statement calls in addition to stack information
- Use `com.sun.jdi` to allow program access to the call stack and "`eip` register"
- Store previous stack frames to implement GDB `prev` and to allow "Step Back" functionality in addition to "Step Forward/Over/Through"
- Proposed frameworks and libraries: JLine3 for CLI, Guava for code tools, TensorFlow for heuristic error analysis, Maven for library and license management, Lombok for boilerplate reduction

## Scenarios

Scenarios will consist of the use cases and the desired functionality of the `jdb` debugger.

### Case 1: Launching `jdb`, exiting `jdb`

``` shell
# Launch JDB
$ java -jar jdb.jar
JDB Debugger - Johnny Cao - CS Topics 2017
Type "h" for help

(jdb) lsjvm
144: Proc name
(jdb) attach 144
Attached to JVM 144: Proc name
(jdb) detach
Detached from JVM 144: Proc name
(jdb) spawn ...
Spawning process for [file]...
(jdb) config initialbreak true
initialbreak = true
(jdb) config inspectonbreak true
inspectonbreak = true
(jdb) spawn ...
Spawning process 6448 for [file]...

Execution stopped at -1
No watched objects found
(jdb) detach
(jdb) exit

$ java -jar jdb.jar --spawn|-s [.java|.class|.jar]
JDB Debugger - Johnny Cao - CS Topics 2017
Type "h" for help

Spawning process 2231 for [file]...

(jdb) sp ~/IdeaProjects/Topics/src/main/java  # Source path
Source path set to: ...
(jdb) context Scenario      # Switch JDB to this FQN filename
Context set to Scenario.java
(jdb) ba Scenario:4         # Breakafter Scenario line 4
Breakpoint set after line 4 in Scenario.java
(jdb) ba 4                  # Breakafter current context line 4
Brekapoint set after line 4 in Scenario.java
(jdb) clearbreaks           # Clear breakpoints
Cleared breakpoint(s): 4
...
(jdb) exit

$
```

``` java
public class Scenario {
    public static void main(String[] args) {
        int a = 3;
        int b = 4;
        int c = a + b;
        Object nil = null;
        nil.hashCode();
    }
}
```

### Case 2: Debug output

```
(jdb) watch 2
FATAL: Not a statement
(jdb) watch 3
FATAL: No sources found
(jdb) sp ...
...
(jdb) watch 3
Watching: int a = 3;
(jdb) inspect
Scenario:3: "3"
(jdb) exit
```
