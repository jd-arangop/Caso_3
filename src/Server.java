
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Server extends Thread{
    public static final int PORT = 3400;

    //Valores P y G generados anteriormente
    private static final BigInteger P = new BigInteger("D49A7AD853F484570E1811CC99D285D3DB6BEA6EF8ECF6D245058590D8EAA7861A512AD05B5416033AF237970E32D4ACB3B271B1009D96F4237C35781A54F7EFD66F7C06A125C21023A270213908836132C9D41151634E45C957018A233A5919C5BAFD9EBE3351F84E5F5623B3C84AA92004399E8137AC8D0D2F2A7C9A38BB57", 16);
    private static final BigInteger G = new BigInteger("2");

    //Llaves Privadas y Publicas  generadas anteriormente
    private static PrivateKey privateKey;
    public static PublicKey publicKey;

    ServerSocket serverSocket= null;
    boolean exceute = true;
    int maxThreads;
    int threadsNumber = 0;

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

    public Server(int pConexiones) throws IOException {
        maxThreads = pConexiones;
        System.out.println("Inicio del servidor prinicipal");

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        ServerHandler.setGP(G, P);

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

            ServerHandler.setKeys(privateKey, publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        
        while (threadsNumber < maxThreads) {
            Socket socket;
            try {
                socket = serverSocket.accept();
    
                ServerHandler serverHandler = new ServerHandler(socket, threadsNumber);
                threadsNumber++;

                serverHandler.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
