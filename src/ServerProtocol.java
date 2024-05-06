import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ServerProtocol {
    //Constantes
    private static final String INIT = "SECURE INIT ";
    private static final String OK = "OK";
    private static final String ERROR = "ERROR";
    private static final String CONTINUAR = "CONTINUAR";
    private static final String GRACIAS = "GRACIAS";

    public static void process(int id, PrintWriter pOut, BufferedReader pIn, PrivateKey privateKey, 
    PublicKey publicKey, BigInteger P, BigInteger G) 
    throws Exception {
        //Inicializacion de variables
        String inputLine = "";
        String outputLine = "";

        //Inicializacion de estado
        int state = 0;

        //Random
        SecureRandom secureRandom = new SecureRandom();

        //Parametros de la sesion
        BigInteger x = BigInteger.ZERO;
        byte[] iv = new byte[16];
        byte[] K_AB1 = new byte[256];
        byte[] K_AB2 = new byte[256];

        while (state < 6 && (inputLine = pIn.readLine()) != null) {

            if (inputLine.equalsIgnoreCase(ERROR)){
                System.out.println("Delegado " + id + ": El cliente cancelo la conexión");
                state = 404;
                break;
            };

            switch (state) {
                case 0:
                    int index = inputLine.indexOf(INIT);
                    if (inputLine.startsWith(INIT) && index != -1) {

                        String retoString = inputLine.substring(index + INIT.length());
                        BigInteger reto = new BigInteger(retoString);

                        //Firma del reto
                        long iniciof = System.nanoTime();
                        Signature signature = Signature.getInstance("SHA256withRSA");
                        signature.initSign(privateKey);
                        signature.update(reto.toByteArray());
                        byte[] sign =  signature.sign();
                        long finf = System.nanoTime();
                        long tiempoff = finf - iniciof;
                        double tiempof = tiempoff/1e9;
                        System.out.println("Delegado " + id + ": Generar firma: " + tiempof + "  SEGUNDOS");

                        //Envia la firma al cliente
                        outputLine = new String(Base64.getEncoder().encode(sign));
                        state++;
                    } else {
                        outputLine = ERROR;
                        state = 404;
                    }
                break;
            
                case 1:
                    if (inputLine.equalsIgnoreCase(OK)){
                        x = new BigInteger(1024, secureRandom);
                        while (P.compareTo(x) <= 0) {
                            x = new BigInteger(1024, secureRandom);
                        }

                        //Calcula Gx = G^x mod P
                        BigInteger Gx = G.modPow(x, P);

                        //Genera iv
                        secureRandom.nextBytes(iv);

                        //Envia G
                        pOut.println(G.toString());

                        //Envia P
                        pOut.println(P.toString());

                        //Envia Gx
                        pOut.println(Gx.toString());

                        //Envia iv
                        pOut.println(new String(Base64.getEncoder().encode(iv)));
                        
                        //Firma de G+P+G^x
                        byte[] data = (G.toString()+P.toString()+Gx.toString()).getBytes();

                        Signature signature = Signature.getInstance("SHA256withRSA");
                        signature.initSign(privateKey);
                        signature.update(data);
                        byte[] sign =  signature.sign();

                        outputLine = new String(Base64.getEncoder().encode(sign));
                        state++;
                    }
                    else {
                        System.out.println("Delegado " + id + ": Inconsisntencia en la información. Terminando conexión");
                        outputLine = ERROR;
                        state = 404;
                    }
                break;
                
                case 2:
                    if (inputLine.equalsIgnoreCase("OK")) {
                        //Recibe Gy
                        BigInteger Gy = new BigInteger(pIn.readLine());

                        //Calcula clave de la sesión
                        BigInteger parteClaveSesion = Gy.modPow(x, P);

                        MessageDigest digest = MessageDigest.getInstance("SHA-512");
                        byte[] claveSesionResumida = digest.digest(parteClaveSesion.toByteArray());

                        //Partir la clave en las claves simetricas K_AB1 y K_AB2
                        K_AB1 = new byte[claveSesionResumida.length/2];
                        K_AB2 = new byte[claveSesionResumida.length/2];
                        System.arraycopy(claveSesionResumida, 0, K_AB1, 0, claveSesionResumida.length / 2);
                        System.arraycopy(claveSesionResumida, claveSesionResumida.length / 2, K_AB2, 0, claveSesionResumida.length / 2);

                        outputLine = CONTINUAR;
                        state++;
                    } else {
                        System.out.println("Delegado " + id + ": Inconsisntencia en la información. Terminando conexión");
                        outputLine = ERROR;
                        state = 404;
                    }
                break;

                case 3:
                    String encryptedLogin = inputLine; 
                    String encryptedPassword = pIn.readLine();

                    byte[] encryptedLoginBytes = Base64.getDecoder().decode(encryptedLogin);
                    byte[] encryptedPasswordBytes = Base64.getDecoder().decode(encryptedPassword);

                    //Descifrar con AES
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    IvParameterSpec ivSpec = new IvParameterSpec(iv);
                    SecretKeySpec keySpec = new SecretKeySpec(K_AB1, "AES");
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

                    //Descifrar el login
                    byte[] decryptedLogin = cipher.doFinal(encryptedLoginBytes);
                    String login = new String(decryptedLogin);

                    //Descifrar el password
                    byte[] decryptedPassword = cipher.doFinal(encryptedPasswordBytes);
                    String password = new String(decryptedPassword);

                    if (login.equalsIgnoreCase("login") && password.equalsIgnoreCase("password")) {
                        outputLine = OK;
                        state++;
                    }
                    else {
                        outputLine = ERROR;
                        state = 404;
                    };
                break;

                case 4:
                    cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    ivSpec = new IvParameterSpec(iv);
                    keySpec = new SecretKeySpec(K_AB1, "AES");
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
                    
                    //Obtener la consulta y el HMAC
                    String encryptedConsult = inputLine;
                    String hmac = pIn.readLine();

                    //Obtener los bytes de la consulta y el HMAC
                    byte[] encryptedConsultBytes = Base64.getDecoder().decode(encryptedConsult);
                    byte[] hmacBytes = Base64.getDecoder().decode(hmac);

                    //Desencrpitar la consulta con AES
                    long inicioc = System.nanoTime();
                    byte[] decryptedConsult = cipher.doFinal(encryptedConsultBytes);
                    String consult = new String(decryptedConsult);
                    long finc = System.nanoTime();
                    long tiempocc = finc - inicioc;
                    double tiempoc = tiempocc/1e9;
                    System.out.println("Delegado  " + id + ": Descifrar consulta: " + tiempoc + "SEGUNDOS");

                    //Verificar el HMAC
                    long iniciohm = System.nanoTime();
                    Mac mac = Mac.getInstance("HmacSHA256");
                    mac.init(new SecretKeySpec(K_AB2, "HmacSHA256"));
                    byte[] calculatedHmac = mac.doFinal(consult.getBytes());
                    long finhm = System.nanoTime();
                    long tiempohm = finhm - iniciohm;
                    double tiempoh = tiempohm/1e9;
                    System.out.println("Delegado " + id + ": Verificar HMAC: " + tiempoh + " SEGUNDOS");

                    if(MessageDigest.isEqual(hmacBytes, calculatedHmac)){

                        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        ivSpec = new IvParameterSpec(iv);
                        keySpec = new SecretKeySpec(K_AB1, "AES");
                        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

                        BigInteger intResponse = new BigInteger(consult).subtract(BigInteger.ONE);
                        String response = intResponse.toString();

                        //Cifra la respuesta con AES
                        byte[] encryptedResponseBytes = cipher.doFinal(response.getBytes());
                        String encryptedResponse = Base64.getEncoder().encodeToString(encryptedResponseBytes);

                        //Calcular HMAC de la consulta
                        byte[] responseHmac = mac.doFinal(response.getBytes());

                        //Enviar la consulta
                        pOut.println(encryptedResponse);

                        //Enviar el HMAC
                        outputLine = (Base64.getEncoder().encodeToString(responseHmac));
                        state++;
                    } else {
                        System.out.println("Delegado " + id + ": Inconsisntencia en la información. Terminando conexión");
                        outputLine = ERROR;
                        state = 404;                        
                    }
                break;
                
                case 5:
                    if (inputLine.equalsIgnoreCase(GRACIAS)){
                        System.out.println("Delegado " + id + ": Consulta finalizada. Terminando conexión");
                        state++;
                        break;
                    } else {
                        System.out.println("Delegado " + id + ": Inconsisntencia en la información. Terminando conexión");
                        outputLine = ERROR;
                        state = 404;
                    }
                break;

                default:
                    System.out.println("Delegado " + id + ": Inconsisntencia en la información. Terminando conexión");
                    outputLine = ERROR;
                    state = 404;
                break;
            }
            pOut.println(outputLine);
        }
    }
}
