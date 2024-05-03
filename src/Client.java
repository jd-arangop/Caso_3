import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static final int PUERTO = 3400;
    public static final String SERVIDOR = "localhost";

    public static void main(String args[]) throws IOException{
        Socket socket = null;
        PrintWriter writer = null;
        BufferedReader reader = null;

        System.out.println("Cliente...");

        try {
            socket = new Socket(SERVIDOR, PUERTO);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()
                )
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(
                System.in
            )
        );

        ClientProtocol.process(stdIn, reader, writer);

        writer.close();
        reader.close();
        socket.close();
        stdIn.close();
    }
    
}
