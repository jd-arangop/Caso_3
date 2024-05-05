import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Random;

public class Client {
    public static final int PUERTO = 3400;
    public static final String SERVIDOR = "localhost";

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

    public static void main(String args[]) throws IOException{
        DiffieHellman diffieHellman = new DiffieHellman();

        BigInteger[] valores = generary();
        BigInteger x = valores[1]; 

        BigInteger[] y = Server.generary();
        BigInteger yserver = y[0];
        
        BigInteger z = diffieHellman.calcularz(yserver, x);
        
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
