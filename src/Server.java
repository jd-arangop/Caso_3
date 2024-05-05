import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Server {
    public static final int PORT = 3400;

    public static BigInteger[] generary(){
        DiffieHellman diffieHellman = new DiffieHellman();

        BigInteger p = diffieHellman.getP();

        BigInteger max = p.subtract(BigInteger.ONE);
        Random random = new Random();
        BigInteger x = new BigInteger(max.bitLength(), random);
        while (x.compareTo(max) >= 0) {
            x = new BigInteger(max.bitLength(), random);
        }

        BigInteger y = diffieHellman.calcularmodp(x);
        BigInteger[] valores = new BigInteger[2];
        valores[0] = y;
        valores[1] = x;

        return valores;
    }
    
    public static void main(String[] args) throws IOException {
        DiffieHellman diffieHellman = new DiffieHellman();

        BigInteger[] valores = generary();
        BigInteger x = valores[1]; 

        BigInteger[] y = Client.generary();
        BigInteger yclient = y[0];

        BigInteger z = diffieHellman.calcularz(yclient, x);

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
