JC=javac
CLASSPATH=.
JFLAGS=-g -classpath $(CLASSPATH)
SOURCES=$(shell find . -name '*.java')

OBJECTS=$(SOURCES:.java=.class)

all: classes
		
.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java


default: classes

classes: $(SOURCES:.java=.class)

run: all
	java Client

jar:
	jar cfe client.jar Client $(shell find . -name '*.class')

clean:
	$(foreach var, $(shell find . -name '*.class'), $(RM) '$(var)';)
	$(RM) *.jar
