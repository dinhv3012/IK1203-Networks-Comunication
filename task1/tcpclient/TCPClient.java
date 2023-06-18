package tcpclient;

import java.net.*;
import java.io.*;

public class TCPClient 
{
    private Integer timeout = null;
    private Integer limit = null;
    private boolean shutdown = false;
    
    //add a constructor for TCPClient that takes three parameters
    public TCPClient(boolean shutdown, Integer timeout, Integer limit) 
    {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }

    public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException 
    {
        Socket clientSocket = new Socket(hostname, port);
        
        

        clientSocket.getOutputStream().write(toServerBytes);
        InputStream input = clientSocket.getInputStream();
        int readBytes;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        
        try {

            while ((readBytes = input.read(buffer)) != -1) {
                result.write(buffer, 0, readBytes);
            }
            clientSocket.close();

            return result.toByteArray();

        } catch (Exception e) 
        {
            throw new IOException("Server Connection Error!");
        }
    }
}
