import java.util.*;

// Room class
class ChatRoom {
  private Set<ChatUser> usersInRoom;
  private String name;

  public ChatRoom(String name) {
    this.usersInRoom = new TreeSet<ChatUser>();
    this.name = name;
  }

  public ChatUser[] getUserArray() {
    return this.usersInRoom.toArray(new ChatUser[this.usersInRoom.size()]);
  }

  public void joinUser(ChatUser user) {
    this.usersInRoom.add(user);
  }

  public void leftUser(ChatUser user) {
    this.usersInRoom.remove(user);
  }

  public String getName() {
    return this.name;
  }
}
