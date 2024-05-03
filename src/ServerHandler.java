import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerHandler extends Thread {
    private Socket clientSocket = null;
    private int id;

    public ServerHandler(Socket pSocket, int pId) {
        this.clientSocket = pSocket;
        this.id = pId;
    }

    @Override
    public void run() {
        System.out.println("Inicio de un nuevo delegado: " + id);

        try {
            PrintWriter writer = new PrintWriter(
                clientSocket.getOutputStream(), 
                true  
            );
            BufferedReader reader = new BufferedReader(
              new InputStreamReader(
                clientSocket.getInputStream()
              )  
            );

            ServerProtocol.process(writer, reader);

            reader.close();
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
