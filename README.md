# Byte Buddy agent weaving advice into bootstrap classes

This project was created by [Antoine Gicquel](https://github.com/Antoine-Gicquel) in order to illustrate
[StackOverflow question #68715127](https://stackoverflow.com/q/68715127/1082681).

[Alexander Kriegisch](https://github.com/kriegaex) forked and restructured it,
  * building with Maven instead of Make,
  * splitting the agent module into a springboard agent and a small set of classes needed by the Byte Buddy advice,
    extracting injecting it from its uber JAR and injecting it into the boot classpath. 
  * I also added a little example application to attach the agent to during runtime, utilising the launcher module -
    attacher would actually be a better name.

What the advice code and its dependency classes actually do according to the original author, is to track the complete
call stack of each thread, including arguments of called methods. _(I did not analyse if this has to be done the way it is done, but simply kept
the functionality as-is, only factored out the map keeping the stats out from the agent class into a separate class,
because the agent is not on the boot classloader.)_ The whole JVM is then instrumented using Frida to call the
`callStackToJSON` method, and this part works great. The Frida part is not contained in this repository.

## How to build

```shell
mvn clean package
```

## How to run

I am assuming that you have a UNIX-like shell with `grep` and `sed` available, e.g. Git Bash for Windows:

### Console 1 - example application

This application simply prints stuff to the console in an endless loop. The log output should change, as soon as the
launcher in console 2 attaches the agent and the latter transforms the application class and some bootstrap classes
using Byte Buddy, logging its activity to the console.

```shell
java -jar application/target/application-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Console 2 - launcher, dynamically attaching agent

Here we start the launcher, telling it which agent _(agent-1.0-SNAPSHOT.jar)_ to attach to which process ID. The latter
is determined by using JDK command line tool `jps`, filtering its output via `jps | grep -E 'application-.*\.jar' |
sed -E 's/^([0-9]+).*/\1/'` in order to get the PID.

```shell
java -jar launcher/target/launcher-1.0-SNAPSHOT-jar-with-dependencies.jar agent/target/agent-1.0-SNAPSHOT.jar $(jps | grep -E 'application-.*\.jar' | sed -E 's/^([0-9]+).*/\1/')
```
