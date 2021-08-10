build:
	javac -cp $(CLASSPATH) *.java
	jar cmvf MANIFEST.MF agent.jar *.class
	rm *.class

clean:
	rm -f *.jar *.class

.PHONY: build clean
