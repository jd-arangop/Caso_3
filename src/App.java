public class App {
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.start();

        Client.publicKey = Server.publicKey;
        Client client = new Client(0);
        client.start();
    }
}
