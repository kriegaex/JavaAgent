# JavaAgent
This is the code for a Java Agent using ByteBuddy, whose purpose is to track the complete call stack of each thread, with every arguments to every parent calls.

The whole JVM is then instrumented using Frida to call the callStackToJSON method, and this part works great.

## Build

`$ cd agent && make build`  
`$ cd ../launcher && make build`

## Run

To run the agent, a JVM must be running, to which it will attach automatically.

`$ cd launcher && make run`
