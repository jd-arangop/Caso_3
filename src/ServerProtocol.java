import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ServerProtocol {
    public static void process(PrintWriter pOut, BufferedReader pIn) throws IOException {
        String inputLine;
        String outputLine;
        int state = 0;

        while (state < 3 && (inputLine = pIn.readLine()) != null) {
            
            System.out.println("Entrada a procesar: " + inputLine);

            switch (state) {
                case 0:
                    if (inputLine.equalsIgnoreCase("HOLA")) {
                        outputLine = "Listo";
                        state++;
                    } else {
                        outputLine = "ERROR. Esperaba Hola";
                        state = 0;
                    }
                    break;
            
                case 1:
                    try {
                        int val = Integer.parseInt(inputLine);
                        val--;
                        outputLine = "" + val;
                        state++;
                    } catch (Exception e) {
                        outputLine = "ERROR en argumento esperado";
                        state = 0;
                    }
                    break;
                
                case 2:
                    if (inputLine.equalsIgnoreCase("OK")) {
                        outputLine = "Adios";
                        state++;
                    } else {
                        outputLine = "ERROR. Esperaba Ok";
                        state = 0;
                    }
                    break;
            
                default:
                    outputLine = "ERROR";
                    state = 0;
                    break;
            }
            pOut.println(outputLine);
        }
    }
}
