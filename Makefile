JFLAGS = -g
JC = javac

SOURCES = \
	ChatUser.java \
	ChatRoom.java \
	ChatMessage.java \
	ChatClient.java \
	ChatServer.java

CLASSES = $(SOURCES:.java=.class)

%.class : %.java
	$(JC) $(JFLAGS) $<

default: $(CLASSES)

clean:
	$(RM) *.class
