JC=javac
CLASSPATH=.
JFLAGS=-g -classpath $(CLASSPATH) 
SRC=.
SOURCES=$(shell find $(SRC) -name '*.java')
OBJECTS=$(SOURCES:.java=.class)
DOCDIR="doc/html/"

all: classes
		
.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

classes: $(OBJECTS)

run: all
	java Launcher

jar: classes
	jar cfe client.jar Launcher *.class util/*.class irc/*.class client/*.class

docs:
	$(RM) -r $(DOCDIR)/*
	javadoc -private -classpath $(SRC) -sourcepath $(SRC) -d $(DOCDIR) *.java util irc client

clean:
	$(foreach var, $(shell find . -name '*.class'), $(RM) '$(var)';)
	$(RM) *.jar
