JC=javac
CLASSPATH=.
JFLAGS=-g -classpath $(CLASSPATH)
SOURCES=$(shell find . -name '*.java')

OBJECTS=$(SOURCES:.java=.class)

DOCDIR="doc/html/"

all: classes
		
.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

default: classes

classes: $(SOURCES:.java=.class)

run: all
	java Launcher

jar:
	jar cfe client.jar Launcher $(shell find . -name '*.class')

docs:
	$(RM) -r $(DOCDIR)/*
	javadoc -linksource -sourcetab 4 -private -classpath . -sourcepath . -d $(DOCDIR)  $(shell find . -name '*.class')

clean:
	$(foreach var, $(shell find . -name '*.class'), $(RM) '$(var)';)
	$(RM) *.jar
