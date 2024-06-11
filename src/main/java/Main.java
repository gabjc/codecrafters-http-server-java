import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    // System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    
    // gets the directory of where the absolute path of the stored files
    String directory = ".";
    if (args.length == 2) { 
      directory = args[1];
    }
    
    System.out.println("Directory: " + directory);
    
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    
    try {
      serverSocket = new ServerSocket(4221);
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);

      // Constantly listen for incoming connections
      while (true) {
        clientSocket = serverSocket.accept(); // Wait for connection from client.

        // Gets the input stream
        InputStream input = clientSocket.getInputStream();
        // Reads text from character input stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(input)); 
        String line = reader.readLine();

        //Testing line output
        System.out.println(line);

        // Splitting the line based on spaces
        String[] HttpRequest = line.split(" ", 0);

        System.out.println("FIRST: " + HttpRequest[0]);

        // Testing HttpRequest outputs
        for (String arr : HttpRequest) {
          System.out.println(arr);
        }

        // Initialized for the write function
        OutputStream output = clientSocket.getOutputStream();

        if (HttpRequest[0].equals("GET")) {
          if (HttpRequest[1].equals("/")) {
            output.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
          } 
          // IMPLEMENTING ENDPOINT AND RESPONDING WITH STRING
          else if (HttpRequest[1].startsWith("/echo/")) {
            // Get the rest of the string after "/echo/"
            String message = HttpRequest[1].substring(6);
            String str = String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s", message.length(), message);
            output.write(str.getBytes());
          } 
          // READING THE USER-AGENT HEADER
          else if (HttpRequest[1].startsWith("/user-agent")) {
            // Skip the Host header
            reader.readLine();
            // Get the User-Agent header
            String userAgent = reader.readLine();
            String body = userAgent.substring(12);
            String str = String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s", body.length(), body);
            output.write(str.getBytes());
          } 
          // RETURNING A FILE
          else if (HttpRequest[1].startsWith("/files")) {
            // Get the file and read from the file
            String fileName = HttpRequest[1].substring(7);
            File file = new File(directory, fileName);
            
            // If it exists, read from it
            if (file.exists()){
              String body = Files.readString(file.toPath());
              String str = String.format("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: %d\r\n\r\n%s", body.length(), body);
              output.write(str.getBytes());
            } else {
                output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
          } 
          else {
            output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
          }

        } else if (HttpRequest[0].equals("POST")) {
          // POST A FILE
          String fileName = HttpRequest[1].substring(7);
          File file = new File(directory + fileName);
          if (file.createNewFile()) {
            // Read the body of the request
            StringBuilder bodyBuffer = new StringBuilder();
            while (reader.ready()) {
              bodyBuffer.append((char)reader.read());
              // System.out.println(bodyBuffer.toString());
            }
            String body = bodyBuffer.toString();

            FileWriter writer = new FileWriter(file);
            System.out.println("Body: " + body);
  
            writer.write(body);
            writer.close();
            output.write("HTTP/1.1 201 Created\r\n\r\n".getBytes());
          } else {
            output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
          }

        }
    }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }    
  }
}


