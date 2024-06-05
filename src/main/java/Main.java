import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    
    try {
      serverSocket = new ServerSocket(4221);
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);
      clientSocket = serverSocket.accept(); // Wait for connection from client.

      // Server will respond with a 200 OK response Stage 2
      // clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
      // System.out.println("accepted new connection");  
      
      // Gets the input stream
      InputStream input = clientSocket.getInputStream();
      // Reads text from character input stream
      BufferedReader reader = new BufferedReader(new InputStreamReader(input)); 
      String line = reader.readLine();
      //Testing
      // System.out.println(line);
      String[] HttpRequest = line.split(" ", 0);
      
      for (int i = 0; i < HttpRequest.length; i++) {
        System.out.print(HttpRequest);
      }
      
      OutputStream output = clientSocket.getOutputStream();


    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }    
  }
}
