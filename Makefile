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

clean:

	$(RM) $(shell find . -name '*.class');
