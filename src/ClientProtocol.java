import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ClientProtocol {
    public static void process(int id, BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut, PublicKey publicKey) 
    throws Exception {

        SecureRandom secureRandom = new SecureRandom();
        BigInteger reto = new BigInteger(1024, secureRandom);

        boolean execute = true;

        while (execute) {
            
            pOut.println("SECURE INIT " + reto);

            String firmaServidor = pIn.readLine();
            Signature firma = Signature.getInstance("SHA256withRSA");
            firma.initVerify(publicKey);
            firma.update(reto.toByteArray());
            boolean verificado = firma.verify(Base64.getDecoder().decode(firmaServidor));

            if (verificado){
                pOut.println("OK");
            } else {
                pOut.println("ERROR");
                execute = false;
                break;
            }

            //Recibir G
            BigInteger G = new BigInteger(pIn.readLine());

            //Recibir P
            BigInteger P = new BigInteger(pIn.readLine());

            //Recibir Gx
            BigInteger Gx = new BigInteger(pIn.readLine());

            //Recibir iv
            byte[] iv = Base64.getDecoder().decode(pIn.readLine());

            //Recibir Firma F(K_w-, (G,P,Gx))
            String firmaStr = pIn.readLine();

            long iniciov = System.nanoTime();
            //Verificar Firma
            firma.initVerify(publicKey);
            firma.update((G.toString() + P.toString() + Gx.toString()).getBytes());
            verificado = firma.verify(Base64.getDecoder().decode(firmaStr));
            long finv = System.nanoTime();
            long tiempov = finv - iniciov;
            double tiempovs = tiempov/1e9;
            System.out.println("Cliente " + id + ": Verificacion de firma: " + tiempovs + " SEGUNDOS");
            if (verificado) {
                pOut.println("OK");
            } else {
                pOut.println("ERROR");
                // Finalizar ejecución si la verificación falla
                execute = false;
                break;
            }

            //Generar llave privada y
            BigInteger y = new BigInteger(1024, secureRandom);
            while (P.compareTo(y) <= 0) {
                y = new BigInteger(1024, secureRandom);
            }

            //Calcula Gy = G^y mod P
            long iniciogy = System.nanoTime();
            BigInteger Gy = G.modPow(y, P);
            long fingy = System.nanoTime();
            long tiempog = fingy - iniciogy;
            double tiempogy = tiempog/1e9;
            System.out.println("Cliente " + id + ": Calcular Gy: " + tiempogy + " SEGUNDOS");
            //Envia Gy
            pOut.println(Gy);

            //Calcula la Clave de la sesión
            BigInteger parteClaveSesion = Gx.modPow(y, P);

            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] claveSesionResumida = digest.digest(parteClaveSesion.toByteArray());

            //Clave simetrica K_AB1
            byte[] K_AB1 = new byte[claveSesionResumida.length / 2];

            //Clave simetrica K_AB2
            byte[] K_AB2 = new byte[claveSesionResumida.length / 2];

            System.arraycopy(claveSesionResumida, 0, K_AB1, 0, claveSesionResumida.length / 2);
            System.arraycopy(claveSesionResumida, claveSesionResumida.length / 2, K_AB2, 0, claveSesionResumida.length / 2);

            boolean canContinue = pIn.readLine().equalsIgnoreCase("CONTINUAR");

            if (!canContinue) {
                System.out.println("Cliente " + id + ": Inconsisntencia en la información. Terminando conexión");
                // Finalizar ejecución si la verificación falla
                execute = false;
                break;
            } 

            //Login y contraseña a enviar
            String login = "login";
            String password = "password";

            //Cifrar con AES
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(K_AB1, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            //Cifrado del login
            byte[] encryptedBytes = cipher.doFinal(login.getBytes());
            String encryptedLogin = Base64.getEncoder().encodeToString(encryptedBytes);

            //Cifrado de la contraseña
            encryptedBytes = cipher.doFinal(password.getBytes());
            String encryptedPassword = Base64.getEncoder().encodeToString(encryptedBytes);

            //Enviar login y contraseña
            pOut.println(encryptedLogin);
            pOut.println(encryptedPassword);

            String response = pIn.readLine();

            if (response.equalsIgnoreCase("OK")){
                // Genera la consulta
                String consult = new BigInteger(1024, secureRandom).toString();

                //Cifra la consulta con AES
                long inicioc = System.nanoTime();
                byte[] encryptedConsultBytes = cipher.doFinal(consult.getBytes());
                String encryptedConsult = Base64.getEncoder().encodeToString(encryptedConsultBytes);
                long finc = System.nanoTime();
                long tiempocc = finc - inicioc;
                double tiempoc = tiempocc/1e9;
                System.out.println("Cliente " + id + ": Cifrar consulta: " + tiempoc + " SEGUNDOS");
                //Calcular HMAC de la consulta
                long iniciohm = System.nanoTime();
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(K_AB2, "HmacSHA256"));
                byte[] hmac = mac.doFinal(consult.getBytes());
                long finhm = System.nanoTime();
                long tiempoh = finhm - iniciohm;
                double tiempohm = tiempoh/1e9;
                System.out.println("Cliente " + id + ": Generar HMAC: " + tiempohm + " SEGUNDOS");

                //Enviar la consulta
                pOut.println(encryptedConsult);

                //Enviar el HMAC
                pOut.println(Base64.getEncoder().encodeToString(hmac));

                //Obtener la respuesta y el HMAC
                String encryptedResponse = pIn.readLine();
                String responseHmac = pIn.readLine();

                //Obtener los bytes de la respuesta y su HMAC
                byte[] encryptedResponseBytes = Base64.getDecoder().decode(encryptedResponse);
                byte[] hmacBytes = Base64.getDecoder().decode(responseHmac);

                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                ivSpec = new IvParameterSpec(iv);
                keySpec = new SecretKeySpec(K_AB1, "AES");
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
                
                //Desencrpitar la consulta con AES
                byte[] decryptedConsult = cipher.doFinal(encryptedResponseBytes);
                response = new String(decryptedConsult);

                //Verificar el HMAC
                byte[] calculatedHmac = mac.doFinal(response.getBytes());

                if(MessageDigest.isEqual(hmacBytes, calculatedHmac)){
                    pOut.println("GRACIAS");
                    execute = false;
                    break;
                } else {
                    pOut.println("ERROR");
                    System.out.println("Cliente " + id + ": Inconsisntencia en la información. Terminando conexión");
                    // Finalizar ejecución si la verificación falla
                    execute = false;
                    break;   
                }

            } else if (response.equalsIgnoreCase("ERROR")) {
                System.out.println("Cliente " + id + ": El servidor encontro inconsistencias.");
                // Finalizar ejecución si la verificación falla
                execute = false;
                break;
            } else {
                System.out.println("Cliente " + id + ": Inconsisntencia en la información. Terminando conexión");
                // Finalizar ejecución si la verificación falla
                execute = false;
                break;   
            }
        }
    }
}
