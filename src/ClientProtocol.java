import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ClientProtocol {
    public static void process(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) 
    throws IOException {

        String fromUser;
        String fromServer;
        
        boolean execute = true;

        while (execute) {
            
            System.out.println("Escriba el mensaje para enviar: ");
            fromUser = stdIn.readLine();

            if (fromUser != null) {
                System.out.println("El usuario escribió: " + fromUser);
                if (fromUser.equalsIgnoreCase("OK")) {
                    execute = false;
                }

                pOut.println(fromUser);
            }

            if ((fromServer = pIn.readLine()) != null){
                System.out.println("Respuesta del servidor: " + fromServer);
            }
        }
    }
}
