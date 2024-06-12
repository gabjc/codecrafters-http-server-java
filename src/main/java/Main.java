import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;


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
    
    // System.out.println("Directory: " + directory);
    
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
        Map<String, String> headers = new HashMap<String, String>();

        //Read the request line
        String line = reader.readLine();

        String header = "";
        //Read the headers of the request
        while ((header = reader.readLine()) != null && !header.equals("") && !header.isEmpty()) {
          String[] headerVal = header.split(":", 2);
          if (headerVal.length == 2) {
            headers.put(headerVal[0], headerVal[1].trim());
          }
        }

          // Iterating HashMap through for loop
          for (Map.Entry<String, String> set :
          headers.entrySet()) {

          // Printing all elements of a Map
          System.out.println(set.getKey() + " = "
                            + set.getValue());
        }

        //Testing line output
        // System.out.println(line);
        // Splitting the line based on spaces
        String[] HttpRequest = line.split(" ", 3);
        String httpMethod = HttpRequest[0];
        String reqTarget = HttpRequest[1];
        String httpVer = HttpRequest[2];

        // System.out.println("FIRST: " + httpMethod);
        // System.out.println("SECOND: " + reqTarget);
        // System.out.println("THIRD: " + httpVer);

        // Initialized for the write function
        OutputStream output = clientSocket.getOutputStream();

        if (httpMethod.equals("GET")) {
          if (reqTarget.equals("/")) {
            output.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
          } 
          // IMPLEMENTING ENDPOINT AND RESPONDING WITH STRING
          else if (reqTarget.startsWith("/echo/")) {
            // Get the rest of the string after "/echo/"
            String str = "";
            if (headers.containsKey("Accept-Encoding") && headers.get("Accept-Encoding").contains("gzip")) {
              String message = reqTarget.substring(6);

              System.out.println(message);

              ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
              GZIPOutputStream gzipOS = new GZIPOutputStream(byteArrayOS);
              gzipOS.write(message.getBytes("UTF-8"));
              byte[] gzipData = byteArrayOS.toByteArray();

              System.out.println(gzipData);

              str = "HTTP/1.1 200 OK\r\nContent-Encoding: gzip\r\nContent-Type: text/plain\r\nContent-Length: " + gzipData.length + "\r\n\r\n";
              output.write(str.getBytes(StandardCharsets.UTF_8));
              output.write(gzipData);

            }
            else {
              String message = reqTarget.substring(6);
              str = String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: %d\r\n\r\n%s", message.length(), message);
              output.write(str.getBytes());
            }
            
          } 
          // READING THE USER-AGENT HEADER
          else if (reqTarget.startsWith("/user-agent")) {
            String str = String.format("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " +  headers.get("User-Agent").length() + "\r\n\r\n" + headers.get("User-Agent"));
            output.write(str.getBytes());
          } 
          // RETURNING A FILE
          else if (reqTarget.startsWith("/files")) {
            // Get the file and read from the file
            String fileName = reqTarget.substring(7);
            File file = new File(directory, fileName);
            
            // If it exists, read from it
            if (file.exists()){
              String fileBody = Files.readString(file.toPath());
              String str = String.format("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: %d\r\n\r\n%s", fileBody.length(), fileBody);
              output.write(str.getBytes());
            } else {
                output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
          } 
          else {
            output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
          }

        } else if (httpMethod.equals("POST")) {

          // Read the body of the request
          StringBuilder bodyBuilder = new StringBuilder();
          while (reader.ready()) {
            bodyBuilder.append((char)reader.read());
            // System.out.println(bodyBuilder.toString());
          }
          String body = bodyBuilder.toString();
          // System.out.println("Body: " + body);

          // POST A FILE
          String fileName = reqTarget.substring(7);
          File file = new File(directory + fileName);
          if (file.createNewFile()) {            
            FileWriter writer = new FileWriter(file);
            writer.write(body);
            writer.close();
            output.write("HTTP/1.1 201 Created\r\n\r\n".getBytes());
          } else {
            output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
          }

            output.flush();
            output.close();

        }
    }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }    
  }
}
  