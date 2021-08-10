build:
	javac -cp $(CLASSPATH) src/*.java
	jar cmvf MANIFEST.MF agent.jar src/*.class
	rm src/*.class

clean:
	rm -f *.jar src/*.class

.PHONY: build clean
