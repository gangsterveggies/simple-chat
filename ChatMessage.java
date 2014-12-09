import java.util.regex.*;

enum MessageType {
  OK, ERROR, MESSAGE, NEWNICK, JOINED, LEFT, BYE, PRIVATE
}

// Message class
public class ChatMessage {
  private MessageType type;
  private String message1;
  private String message2;

  // Regex for command process
  static private final String okRegex = "OK";
  static private final String errorRegex = "ERROR";
  static private final String messageRegex = "MESSAGE .+ .+";
  static private final String newnickRegex = "NEWNICK .+ .+";
  static private final String joinedRegex = "JOINED .+";
  static private final String leftRegex = "LEFT .+";
  static private final String byeRegex = "BYE";
  static private final String privateRegex = "PRIV .+ .+";


  public ChatMessage(MessageType type) {
    this.type = type;
    this.message1 = "";
    this.message2 = "";
  }

  public ChatMessage(MessageType type, String message1) {
    this.type = type;
    this.message1 = message1;
    this.message2 = "";
  }

  public ChatMessage(MessageType type, String message1, String message2) {
    this.type = type;
    this.message1 = message1;
    this.message2 = message2;
  }

  public MessageType getType() {
    return this.type;
  }

  public String toString() {
    return this.toString(false);
  }

  public String toString(Boolean pretty) {
    String output = "";
    
    switch (this.type) {
      case OK:
	if (pretty) {
	  output = "Command successful";
	} else {
	  output = "OK";
	}
        break;
      case ERROR:
	if (pretty) {
	  output = "Error: " + this.message1;
	} else {
	  output = "ERROR";
	}
        break;
      case MESSAGE:
	if (pretty) {
	  output = this.message1 + ": " + this.message2;
	} else {
	  output = "MESSAGE " + this.message1 + " " + this.message2;
	}
        break;
      case NEWNICK:
	if (pretty) {
	  output = this.message1 + " is now known as " + this.message2;
	} else {
	  output = "NEWNICK " + this.message1 + " " + this.message2;
	}
        break;
      case JOINED:
	if (pretty) {
	  output = this.message1 + " has joined the room";
	} else {
	  output = "JOINED " + this.message1;
	}
        break;
      case LEFT:
	if (pretty) {
	  output = this.message1 + " left the room";
	} else {
	  output = "LEFT " + this.message1;
	}
        break;
      case BYE:
	if (pretty) {
	  output = "Exiting...";
	} else {
	  output = "BYE";
	}
        break;
      case PRIVATE:
	if (pretty) {
	  output = "<" + this.message1 + ">: " + this.message2;
	} else {
	  output = "PRIVATE " + this.message1 + " " + this.message2;
	}
        break;
    }

    return output + "\n";
  }

  public static ChatMessage parseString(String text) {
    MessageType type;
    String message1 = "";
    String message2 = "";

    if (Pattern.matches(okRegex, text)) {
      type = MessageType.OK;
    } else if (Pattern.matches(errorRegex, text)) {
      type = MessageType.ERROR;
    } else if (Pattern.matches(messageRegex, text)) {
      type = MessageType.MESSAGE;
      message1 = text.split(" ")[1];
      int position = text.substring(7).indexOf(message1);
      message2 = text.substring(7 + position + message1.length());
    } else if (Pattern.matches(newnickRegex, text)) {
      type = MessageType.NEWNICK;
      message1 = text.split(" ")[1];
      message2 = text.split(" ")[2];
    } else if (Pattern.matches(joinedRegex, text)) {
      type = MessageType.JOINED;
      message1 = text.split(" ")[1];
    } else if (Pattern.matches(leftRegex, text)) {
      type = MessageType.LEFT;
      message1 = text.split(" ")[1];
    } else if (Pattern.matches(byeRegex, text)) {
      type = MessageType.BYE;
    } else if (Pattern.matches(privateRegex, text)) {
      type = MessageType.PRIVATE;
      message1 = text.split(" ")[1];
      int position = text.substring(7).indexOf(message1);
      message2 = text.substring(7 + position + message1.length());
    } else {
      type = MessageType.ERROR;
    }

    return new ChatMessage(type, message1, message2);
  }
}
