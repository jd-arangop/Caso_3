import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static final int PORT = 3400;
    private static final BigInteger P = BigInteger.ZERO;
    private static final BigInteger G = BigInteger.ZERO;

    public BigInteger getPublicKey() {
        return P;
    }

    public BigInteger getPrivateKey() {
        return G;
    }
    
    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket= null;
        boolean exceute = true;
        int threadsNumber = 0;

        System.out.println("Inicio del servidor prinicipal");

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        while (exceute) {
            Socket socket = serverSocket.accept();

            ServerHandler serverHandler = new ServerHandler(socket, threadsNumber);
            threadsNumber++;

            serverHandler.start();
        }

        serverSocket.close();
    }
}
