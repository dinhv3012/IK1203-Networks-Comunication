package tcpclient;

import java.net.*;
import java.io.*;

public class TCPClient {

    
    private Integer timeout = null;
    private Integer limit = null;
    private boolean shutdown = false;

    
    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }

    public byte[] askServer(String hostname, int port, byte[] toServerBytes) throws IOException {

        Socket clientSocket = new Socket(hostname, port);
        InputStream input = clientSocket.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int readBytes;
        byte[] buffer = new byte[1024];

        try {
            clientSocket.getOutputStream().write(toServerBytes);
            if (shutdown)
                clientSocket.shutdownOutput();

            if (this.timeout != null)
                clientSocket.setSoTimeout(this.timeout);

            if (this.limit != null) {
                int maxBytes = 0;

                if (this.limit < 1024)
                    buffer = new byte[this.limit];

                while ((readBytes = input.read(buffer)) != -1) {
                    result.write(buffer, 0, readBytes);
                    maxBytes += readBytes;
                    if (maxBytes >= this.limit)
                        break;
                    if (maxBytes + 1024 > this.limit)
                        buffer = new byte[this.limit - maxBytes];
                }
            } else {

                for (readBytes = input.read(buffer); readBytes != -1; readBytes = input.read(buffer)) {
                    result.write(buffer, 0, readBytes);
                }
            }

        } catch (SocketTimeoutException e) {
        }
        clientSocket.close();
        return result.toByteArray();
    }

}
