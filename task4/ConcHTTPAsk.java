import java.net.*;
import java.io.*;

public class ConcHTTPAsk {
    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            ServerSocket serverSocket = new ServerSocket(port);

            for (;;) {

                Socket clientSocket = serverSocket.accept();
                Runnable ConcThread = new MyRunnable(clientSocket);
                new Thread(ConcThread).start();  
            }

        } catch (ArrayIndexOutOfBoundsException | IOException e) {
            System.exit(1);
        }
    }
}
