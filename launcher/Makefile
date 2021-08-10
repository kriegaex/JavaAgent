build:
	javac -cp $(CLASSPATH) src/*.java
	jar cmvf MANIFEST.MF launcher.jar src/*.class
	rm src/*.class

run:
	java -Djava.compiler=NONE -jar launcher.jar /workdir/agent/agent.jar $(shell pidof java)

clean:
	rm -f *.jar *.class

.PHONY: build run clean
