import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class MyRunnable implements Runnable {

    private Socket clientSocket;
    private StringBuilder responseBuilder = new StringBuilder();
    private String hostname = "";
    private Integer clientPort = null;
    private byte[] bytesToServer = new byte[0];
    private Integer timeout = null;
    private Integer limit = null;
    private boolean shutdown = false;

    public MyRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            String input = readRequestFromClient();

            if (isValidRequest(input)) {
                this.responseBuilder.append("HTTP/1.1 400 Bad Request\r\n");
                throw new Exception("Bad Request");
            }

            parseRequestParameters(input);
            sendRequestToServer();
            closeSocket();

        } catch (Exception e) {
            e.printStackTrace();
            closeSocket();
        }
    }

    private String readRequestFromClient() throws IOException {
        byte[] buffer = new byte[1024];
        this.clientSocket.getInputStream().read(buffer);
        String decodedRequestString = new String(buffer, StandardCharsets.UTF_8);

        return decodedRequestString;
    }

    private boolean isValidRequest(String input) {
        String[] request = input.split(" ");
        return (!request[0].equals("GET") || !input.contains("HTTP/1.1"));
    }

    private void parseRequestParameters(String input) throws Exception {
        String request = input.split(" ")[1];
        String[] params = request.split("\\?");

        if (params.length > 0 && params[0].equals("/ask")) {
            if (params.length < 2) {
                this.responseBuilder.append("HTTP/1.1 400 Bad Request\r\n");
                throw new Exception("Bad Request");
            }

            String[] paramList = params[1].split("&");

            for (String param : paramList) {
                String[] paramsPairs = param.split("=");

                if (paramsPairs.length < 2) {
                    this.responseBuilder.append("HTTP/1.1 400 Bad Request\r\n");
                    throw new Exception("Bad Request");
                }

                switch (paramsPairs[0]) {
                    case "shutdown":
                        this.shutdown = Boolean.parseBoolean(paramsPairs[1]);
                        break;
                    case "timeout":
                        this.timeout = Integer.parseInt(paramsPairs[1]);
                        break;
                    case "limit":
                        this.limit = Integer.parseInt(paramsPairs[1]);
                        break;
                    case "hostname":
                        this.hostname = paramsPairs[1];
                        break;
                    case "port":
                        this.clientPort = Integer.parseInt(paramsPairs[1]);
                        break;
                    case "string":
                        this.bytesToServer = paramsPairs[1].getBytes();
                        break;
                    default:
                        break;
                }
            }

        } else {
            this.responseBuilder.append("HTTP/1.1 404 Not Found\r\n");
            throw new Exception("Not Found");
        }

        if (this.hostname.equals("") || this.clientPort == null) {
            this.responseBuilder.append("HTTP/1.1 400 Bad Request\r\n");
            throw new Exception("Bad Request");
        }
    }

    private void sendRequestToServer() throws Exception {
        try {
            TCPClient client = new TCPClient(this.shutdown, this.timeout, this.limit);

            String request = new String(client.askServer(this.hostname, this.clientPort, this.bytesToServer));

            this.responseBuilder.append("HTTP/1.1 200 OK\r\n\r\n");
            this.responseBuilder.append(request);

        } catch (Exception e) {
            this.responseBuilder.append("HTTP/1.1 500 Internal Server Error\r\n");
            throw new Exception("Internal Server Error");
        }
    }

    private void closeSocket() {
        try {
            this.clientSocket.getOutputStream().write(this.responseBuilder.toString().getBytes());
            this.clientSocket.close();
        } catch (IOException e) {

        }
    }
}
