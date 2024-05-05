import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ClientProtocol {
    public static void process(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) 
    throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {

        String fromUser;
        String fromServer;

        SecureRandom secureRandom = new SecureRandom();
        BigInteger reto = new BigInteger(1024, secureRandom);

        boolean execute = true;

        while (execute) {
            
            pOut.println("SECURE INIT " + reto);

            String firmaServidor = pIn.readLine();
            BigInteger clavePublicaServer = ObtenerClaveServidor();
            Signature firma = Signature.getInstance("SHA256withRSA");
            firma.initVerify(Obtener(clavePublicaServer));
            firma.update(reto.toByteArray());
            boolean verificado = firma.verify(Base64.getDecoder().decode(firmaServidor));

            if (verificado){
                pOut.println("OK");
            } else {
                pOut.println("ERROR");
                execute = false;
                break;
            }
            String[] parametrosServidor = pIn.readLine().split(" ");
            BigInteger G = new BigInteger(parametrosServidor[0]);
            BigInteger P = new BigInteger(parametrosServidor[1]);
            BigInteger Gx = new BigInteger(parametrosServidor[2]);
            byte[] iv = Base64.getDecoder().decode(parametrosServidor[3]);
            String firmaStr = parametrosServidor[4];

            firma.initVerify(Obtener(clavePublicaServer));
            firma.update((G.toString() + P.toString() + Gx.toString()).getBytes());
            verificado = firma.verify(Base64.getDecoder().decode(firmaStr));

            if (verificado) {
                pOut.println("OK");
            } else {
                pOut.println("ERROR");
                // Finalizar ejecución si la verificación falla
                execute = false;
                break;
            }

            BigInteger y = new BigInteger(1024, secureRandom);

            BigInteger Gy = G.modPow(y, P);

            pOut.println(Gy);

            BigInteger parteClaveSesion = Gx.modPow(y, P);

            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] claveSesionResumida = digest.digest(parteClaveSesion.toByteArray());

            byte[] K_AB1 = new byte[claveSesionResumida.length / 2];
            byte[] K_AB2 = new byte[claveSesionResumida.length / 2];
            System.arraycopy(claveSesionResumida, 0, K_AB1, 0, claveSesionResumida.length / 2);
            System.arraycopy(claveSesionResumida, claveSesionResumida.length / 2, K_AB2, 0, claveSesionResumida.length / 2);

            execute = false;
        }
    }
    
    public static BigInteger ObtenerClaveServidor() {
        BigInteger[] yserver = Server.generary();
        BigInteger c = yserver[0];
        return c;
    }

    public static PublicKey Obtener(BigInteger c) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] pbytes = c.toByteArray();
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pbytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
