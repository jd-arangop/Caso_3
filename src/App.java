import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Por favor, ingresa la cantidad de delegados deseados: ");
        int delegados = scanner.nextInt();

        Server server = new Server();
        server.start();

        Client.publicKey = Server.publicKey;

        for (int i = 0; i < delegados; i++){
            Client client = new Client(0);
            client.start();
        }
        scanner.close();
    }
}
