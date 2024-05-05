import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

public class ServerHandler extends Thread {
    //P y G
    private static BigInteger G;
    private static BigInteger P;
    
    //Llaves
    private static PrivateKey privateKey;
    private static PublicKey publicKey;

    //Identificadores del delegado
    private Socket clientSocket = null;
    private int id;

    public ServerHandler(Socket pSocket, int pId) {
        this.clientSocket = pSocket;
        this.id = pId;
    }

    public static void setKeys(PrivateKey pPrivate, PublicKey pPublic){
        privateKey = pPrivate;
        publicKey = pPublic;
    }

    public static void setGP(BigInteger pG, BigInteger pP){
        G = pG;
        P = pP;
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

            ServerProtocol.process(id, writer, reader, privateKey, publicKey, P, G);

            reader.close();
            writer.close();
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
