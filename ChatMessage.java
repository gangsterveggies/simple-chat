enum MessageType {
  OK, ERROR, MESSAGE, NEWNICK, JOINED, LEFT, BYE, PRIVATE
}

// Message class
public class ChatMessage {
  private MessageType type;
  private String message1;
  private String message2;

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
    String output = "";
    
    switch (this.type) {
      case OK:
        output = "OK";
        break;
      case ERROR:
        output = "ERROR";
        break;
      case MESSAGE:
        output = "MESSAGE " + message1 + " " + message2;
        break;
      case NEWNICK:
        output = "NEWNICK " + message1 + " " + message2;
        break;
      case JOINED:
        output = "JOINED " + message1;
        break;
      case LEFT:
        output = "LEFT " + message1;
        break;
      case BYE:
        output = "BYE";
        break;
      case PRIVATE:
        output = "PRIVATE " + message1 + " " + message2;
        break;
    }

    return output;
  }
}
