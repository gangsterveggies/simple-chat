import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

// Main server class
public class ChatServer {
  // Buffer for incoming data
  static private final ByteBuffer buffer = ByteBuffer.allocate(16384);

  // Decoder and enconder for transmitting text
  static private final Charset charset = Charset.forName("UTF8");
  static private final CharsetDecoder decoder = charset.newDecoder();
  static private final CharsetEncoder encoder = charset.newEncoder();

  // Regex for command process
  static private final String nickRegex = "nick .+";
  static private final String joinRegex = "join .+";
  static private final String leaveRegex = "leave.*";
  static private final String byeRegex = "bye.*";
  static private final String privateRegex = "priv .+ .+";

  // Users info
  static private HashMap<SocketChannel, ChatUser> userMap = new HashMap<SocketChannel, ChatUser>();
  static private HashMap<String, ChatUser> usernames = new HashMap<String, ChatUser>();
  static private HashMap<String, ChatRoom> roomMap = new HashMap<String, ChatRoom>();

  static public void main(String args[]) throws Exception {
    // Port to listen
    int port = Integer.parseInt(args[0]);
    
    try {
      // Setup server sockets
      ServerSocketChannel ssc = ServerSocketChannel.open();
      ssc.configureBlocking(false);

      ServerSocket ss = ssc.socket();
      InetSocketAddress isa = new InetSocketAddress(port);
      ss.bind(isa);

      Selector selector = Selector.open();

      ssc.register(selector, SelectionKey.OP_ACCEPT);
      System.out.println("Chat listening on port " + port);

      // Listen loop
      while (true) {
        int num = selector.select();

        // No activity
        if (num == 0) {
          continue;
        }

        // Process keys of detected activity
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> it = keys.iterator();
        
        while (it.hasNext()) {
          SelectionKey key = it.next();

          if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
            // New incoming connection
            Socket s = ss.accept();
            System.out.println("Got connection from " + s);

            SocketChannel sc = s.getChannel();
            sc.configureBlocking(false);

            sc.register(selector, SelectionKey.OP_READ);
            userMap.put(sc, new ChatUser(sc));
          } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
            // Incoming data on a connection
            SocketChannel sc = null;

            try {
              sc = (SocketChannel) key.channel();
              boolean ok = processInput(sc);

              // Close dead connections
              if (!ok) {
                key.cancel();
                closeClient(sc);
              }

            } catch (IOException ie) {
              // On error close client
              key.cancel();
              closeClient(sc);
            }
          }
        }

        keys.clear();
      }
    } catch (IOException ie) {
      System.err.println(ie);
    }
  }

  // Helper function to close client
  static private void closeClient(SocketChannel sc) {
    Socket s = sc.socket();
    try {
      System.out.println("Closing connection to " + s);
      sc.close();
    } catch (IOException ie) {
      System.err.println("Error closing socket " + s + ": " + ie);
    }

    if (!userMap.containsKey(sc)) {
      return;
    }

    ChatUser sender = userMap.get(sc);
    if (sender.getState() == State.INSIDE) {
      ChatRoom room = sender.getRoom();
      room.leftUser(sender);
      ChatUser[] userList = room.getUserArray();

      for (ChatUser user : userList) {
        try {
          sendLeftMessage(user, sender.getUsername());
        } catch (IOException ie) {
          System.err.println("Error sending left message: " + ie);
        }
      }

      if (userList.length == 0) {
        roomMap.remove(room.getName());
      }
    }

    usernames.remove(sender.getUsername());
    userMap.remove(sc);
  }

  // Helper function to send a message
  static private void sendMessage(SocketChannel sc, ChatMessage message) throws IOException {
    sc.write(encoder.encode(CharBuffer.wrap(message.toString())));
  }

  // Send message message
  static private void sendMessageMessage(ChatUser receiver, String sender, String messageValue) throws IOException {
    ChatMessage message = new ChatMessage(MessageType.MESSAGE, sender, messageValue);
    sendMessage(receiver.getSocket(), message);
  }

  // Send error message
  static private void sendErrorMessage(ChatUser receiver, String errorMessage) throws IOException {
    ChatMessage message = new ChatMessage(MessageType.ERROR, errorMessage);
    sendMessage(receiver.getSocket(), message);
  }

  // Send ok message
  static private void sendOkMessage(ChatUser receiver) throws IOException {
    ChatMessage message = new ChatMessage(MessageType.OK);
    sendMessage(receiver.getSocket(), message);
  }

  // Send newnick message
  static private void sendNewnickMessage(ChatUser receiver, String oldNick, String newNick) throws IOException {
    ChatMessage message = new ChatMessage(MessageType.NEWNICK, oldNick, newNick);
    sendMessage(receiver.getSocket(), message);
  }

  // Send joined message
  static private void sendJoinedMessage(ChatUser receiver, String joinNick) throws IOException {
    ChatMessage message = new ChatMessage(MessageType.JOINED, joinNick);
    sendMessage(receiver.getSocket(), message);
  }

  // Send left message
  static private void sendLeftMessage(ChatUser receiver, String leftNick) throws IOException {
    ChatMessage message = new ChatMessage(MessageType.LEFT, leftNick);
    sendMessage(receiver.getSocket(), message);
  }

  // Send bye message
  static private void sendByeMessage(ChatUser receiver) throws IOException {
    ChatMessage message = new ChatMessage(MessageType.BYE);
    sendMessage(receiver.getSocket(), message);
  }

  // Send private message
  static private void sendPrivateMessage(ChatUser receiver, String sender, String messageValue) throws IOException {
    ChatMessage message = new ChatMessage(MessageType.PRIVATE, sender, messageValue);
    sendMessage(receiver.getSocket(), message);
  }

  // Send simple message
  static private void sendSimpleMessage(ChatUser sender, String messageValue) throws IOException {
    if (sender.getState() == State.INSIDE) {
      ChatRoom senderRoom = sender.getRoom();
      ChatUser[] userList = senderRoom.getUserArray();

      for (ChatUser user : userList) {
        sendMessageMessage(user, sender.getUsername(), messageValue);
      }
    } else {
      sendErrorMessage(sender, "You are not in a room");
    }
  }

  static private void sendNickCommand(ChatUser sender, String nick) throws IOException {
    if (usernames.containsKey(nick)) {
      sendErrorMessage(sender, "There already is a user with nick " + nick);
    } else {
      if (sender.getState() == State.INIT) {
        sender.setState(State.OUTSIDE);
      }
      
      if (sender.getState() == State.INSIDE) {
        ChatRoom senderRoom = sender.getRoom();
        ChatUser[] userList = senderRoom.getUserArray();

        for (ChatUser user : userList) {
          if (user != sender) {
            sendNewnickMessage(user, sender.getUsername(), nick);
          }
        }
      }

      usernames.remove(sender.getUsername());
      usernames.put(nick, sender);
      sendOkMessage(sender);
      sender.setUsername(nick);
    }
  }

  static private void sendJoinCommand(ChatUser sender, String roomName) throws IOException {
    if (sender.getState() == State.INIT) {
      sendErrorMessage(sender, "Authentication required");
    } else {
      if (!roomMap.containsKey(roomName)) {
        roomMap.put(roomName, new ChatRoom(roomName));
      }
      
      ChatRoom newRoom = roomMap.get(roomName);
      ChatUser[] newRoomUserList = newRoom.getUserArray();
      newRoom.joinUser(sender);

      for (ChatUser user : newRoomUserList) {
        sendJoinedMessage(user, sender.getUsername());
      }

      if (sender.getState() == State.INSIDE) {
        ChatRoom oldRoom = sender.getRoom();
        oldRoom.leftUser(sender);
        ChatUser[] oldRoomUserList = oldRoom.getUserArray();

        for (ChatUser user : oldRoomUserList) {
          sendLeftMessage(user, sender.getUsername());
        }
      }

      sendOkMessage(sender);
      sender.setRoom(newRoom);
      sender.setState(State.INSIDE);
    }
  }

  static private void sendLeaveCommand(ChatUser sender) throws IOException {
    if (sender.getState() != State.INSIDE) {
      sendErrorMessage(sender, "You are not in a room");
    } else {
      ChatRoom room = sender.getRoom();
      room.leftUser(sender);
      ChatUser[] userList = room.getUserArray();

      for (ChatUser user : userList) {
        sendLeftMessage(user, sender.getUsername());
      }

      if (userList.length == 0) {
        roomMap.remove(room.getName());
      }

      sender.setState(State.OUTSIDE);
    }
  }

  // Send bye command
  static private void sendByeCommand(ChatUser sender) throws IOException {
    closeClient(sender.getSocket());
    sendByeMessage(sender);
  }

    // Send private message
  static private void sendPrivateCommand(ChatUser sender, String receiver, String messageValue) throws IOException {
    if (sender.getState() == State.INIT) {
      sendErrorMessage(sender, "Authentication required");
    } else {
      if (usernames.containsKey(receiver)) {
        sendOkMessage(sender);
        sendPrivateMessage(usernames.get(receiver), sender.getUsername(), messageValue);
      } else {
        sendErrorMessage(sender, receiver + ": No such nickname online");
      }
    }
  }
  
  // Process received data
  static private boolean processInput(SocketChannel sc) throws IOException {
    buffer.clear();
    sc.read(buffer);
    buffer.flip();

    if (buffer.limit() == 0) {
      return false;
    }

    // Decode and send message back
    String message = decoder.decode(buffer).toString().trim();
    ChatUser sender = (ChatUser)userMap.get(sc);

    if (message.startsWith("/")) {
      String escapedMessage = message.substring(1);
      String command = escapedMessage.trim();
      
      if (Pattern.matches(nickRegex, command)) {
        sendNickCommand(sender, command.split(" ")[1]);
      } else if (Pattern.matches(joinRegex, command)) {
        sendJoinCommand(sender, command.split(" ")[1]);
      } else if (Pattern.matches(leaveRegex, command)) {
        sendLeaveCommand(sender);
      } else if (Pattern.matches(byeRegex, command)) {
        sendByeCommand(sender);
      } else if (Pattern.matches(privateRegex, command)) {
        sendPrivateCommand(sender, command.split(" ")[1], command.split(" ")[2]);
      } else if (command.startsWith("/")) {
        sendSimpleMessage(sender, escapedMessage);
      } else {
	sendErrorMessage(sender, "Unknown command");
      }
    } else {
      sendSimpleMessage(sender, message);
    }

    return true;
  }
}
