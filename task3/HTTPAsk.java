import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static java.lang.System.*;

public class HTTPAsk {
    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            ServerSocket serverSocket = new ServerSocket(port);

            for (;;) {
                Socket clientSocket = serverSocket.accept();
                OutputStream socketOutput = clientSocket.getOutputStream();
                StringBuilder responseBuilder = new StringBuilder();

                try {
                    byte[] buffer = new byte[1024];
                    clientSocket.getInputStream().read(buffer);
                    String decodedRequestString = new String(buffer, StandardCharsets.UTF_8);

                    if (!decodedRequestString.split(" ")[0].equals("GET")
                            || !decodedRequestString.contains("HTTP/1.1")) {
                        responseBuilder.append("HTTP/1.1 400 Bad Request\r\n");
                        throw new Exception("Bad Request");
                    }

                    String url = decodedRequestString.split(" ")[1];
                    String[] params = url.split("\\?");

                    if (params.length > 0 && params[0].equals("/ask")) {
                        if (params.length < 2) {
                            responseBuilder.append("HTTP/1.1 400 Bad Request\r\n");
                            throw new Exception("Bad Request");
                        }

                        String[] paramList = params[1].split("&");

                        String hostname = "";
                        int clientPort = 0;
                        byte[] bytesToServer = new byte[0];
                        Integer timeout = null;
                        Integer limit = null;
                        boolean shutdown = false;

                        for (String param : paramList) {
                            String[] paramsPair = param.split("=");

                            if (paramsPair.length < 2) {
                                responseBuilder.append("HTTP/1.1 400 Bad Request\r\n");
                                throw new Exception("Bad Request");
                            }

                            switch (paramsPair[0]) {
                                case "shutdown":
                                    shutdown = Boolean.parseBoolean(paramsPair[1]);
                                    break;
                                case "timeout":
                                    timeout = Integer.parseInt(paramsPair[1]);
                                    break;
                                case "limit":
                                    limit = Integer.parseInt(paramsPair[1]);
                                    break;
                                case "hostname":
                                    hostname = paramsPair[1];
                                    break;
                                case "port":
                                    clientPort = Integer.parseInt(paramsPair[1]);
                                    break;
                                case "string":
                                    bytesToServer = paramsPair[1].getBytes();
                                    break;
                                default:
                                    responseBuilder.append("HTTP/1.1 400 Bad Request\r\n");
                                    throw new Exception("Bad Request");
                            }
                        }

                        if (hostname.equals("") || clientPort == 0) {
                            responseBuilder.append("HTTP/1.1 400 Bad Request\r\n");
                            throw new Exception("Bad Request");
                        }

                        try {
                            responseBuilder.append("HTTP/1.1 200 OK\r\n\r\n");

                            TCPClient client = new TCPClient(shutdown, timeout, limit);
                            responseBuilder.append(new String(client.askServer(hostname, clientPort, bytesToServer)));

                        } catch (Exception e) {
                            responseBuilder.append("HTTP/1.1 500 Internal Server Error\r\n");
                            throw new Exception("Internal Server Error");
                        }

                    } else {
                        responseBuilder.append("HTTP/1.1 404 Not Found\r\n");
                        throw new Exception("Not Found");
                    }

                    socketOutput.write(responseBuilder.toString().getBytes());
                    clientSocket.close();

                } catch (Exception e) {
                    socketOutput.write(responseBuilder.toString().getBytes());
                    clientSocket.close();

                }
            }

        } catch (ArrayIndexOutOfBoundsException e) {

            System.exit(1);

        } catch (IOException e) {

            System.exit(1);
        }
    }
}
