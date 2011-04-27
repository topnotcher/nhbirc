JC=javac
CLASSPATH=.
JFLAGS=-g -classpath $(CLASSPATH)
SOURCES=$(shell ls *.java)

OBJECTS=$(SOURCES:.java=.class)

all: clean classes
		
.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java


default: classes

classes: $(SOURCES:.java=.class)

clean:
	$(RM) *.class
