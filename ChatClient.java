import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Main client class
public class ChatClient {
  // GUI setup
  JFrame frame = new JFrame("Chat Client");
  private JTextField chatBox = new JTextField();
  private JTextArea chatArea = new JTextArea();

  // Socket info
  private SocketChannel clientSocket;
  private ByteBuffer buffer = ByteBuffer.allocate(16384);
  
  // Decoder and enconder for transmitting text
  private final Charset charset = Charset.forName("UTF8");
  private final CharsetDecoder decoder = charset.newDecoder();
  private final CharsetEncoder encoder = charset.newEncoder();
    
  // Show message in GUI
  public void printMessage(final String message) {
    chatArea.append(message);
  }

  // Initializes the GUI and sets up the server connection
  public ChatClient(String server, int port) throws IOException {
    // GUI information
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(chatBox);
    frame.setLayout(new BorderLayout());
    frame.add(panel, BorderLayout.SOUTH);
    frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
    frame.setSize(500, 300);
    frame.setVisible(true);
    chatArea.setEditable(false);
    chatBox.setEditable(true);
    chatBox.requestFocus();
    chatBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          newMessage(chatBox.getText());
        } catch (IOException ex) {
        } finally {
          chatBox.setText("");
        }
      }
    });

    // Socket information
    try {
      clientSocket = SocketChannel.open();
      clientSocket.configureBlocking(false);
      clientSocket.connect(new InetSocketAddress(server, port));
//      outputStream = new DataOutputStream(clientSocket.getOutputStream());
//      inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    } catch (IOException ex) {
    }
  }

  // Sends message to server
  public void newMessage(String message) throws IOException {
    clientSocket.write(encoder.encode(CharBuffer.wrap(message)));
  }

    
  // Processes server input
  public void run() throws IOException {
    while (!clientSocket.finishConnect()) {
    }
    
    // Listen loop
    while (true) {
      buffer.clear();
      clientSocket.read(buffer);
      buffer.flip();

      if (buffer.limit() == 0) {
        continue;
      }

      String message = decoder.decode(buffer).toString().trim();
      printMessage(message + "\n");

      if (message.compareTo("BYE") == 0) {
        break;
      }
    }

    clientSocket.close();
  }

  // Starts the GUI and connections
  public static void main(String[] args) throws IOException {
    ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
    client.run();
    System.exit(0);
  }
}
