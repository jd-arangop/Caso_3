import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

public class Client extends Thread{
    public static final int PUERTO = 3400;
    public static final String SERVIDOR = "localhost";

    public static PublicKey publicKey;

    private int id;

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

    public Client(int pId) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException{
        id = pId;
    }
 
    @Override
    public void run() {
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
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(
                System.in
            )
            );
        
            ClientProtocol.process(id, stdIn, reader, writer, publicKey);

            writer.close();
            reader.close();
            socket.close();
            stdIn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
