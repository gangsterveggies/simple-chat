import java.nio.channels.*;

// User state
enum State {
  INIT, OUTSIDE, INSIDE
}

// User class
public class ChatUser implements Comparable<ChatUser>{
  private String username;
  private State state;
  private ChatRoom room;
  private SocketChannel socket;

  public ChatUser(SocketChannel socket, String username, State state) {
    this.username = username;
    this.state = state;
    this.socket = socket;
  }

  public ChatUser(SocketChannel socket, String username) {
    this.username = username;
    this.state = State.INIT;
    this.socket = socket;
  }

  public ChatUser(SocketChannel socket, State state) {
    this.username = "";
    this.state = state;
    this.socket = socket;
  }

  public ChatUser(SocketChannel socket) {
    this.username = "";
    this.state = State.INIT;
    this.socket = socket;
  }

  @Override
  public int compareTo(ChatUser other){
    return this.username.compareTo(other.username);
  }


  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public State getState() {
    return this.state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public ChatRoom getRoom() {
    return this.room;
  }

  public void setRoom(ChatRoom room) {
    this.room = room;
  }

  public SocketChannel getSocket() {
    return this.socket;
  }
}
