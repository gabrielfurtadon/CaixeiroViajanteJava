package distribuido;
import java.io.*;
import java.net.*;
import java.util.Arrays;

public class ClienteCaixeiro {
    public static void main(String[ ] args) {
    	int port = 12345;
        String serverAddress = "localhost";
        

        try (Socket socket = new Socket(serverAddress, port);
        		
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            System.out.println("Conexão ok!");

            // Receber os dados do servidor
            String[] shortestPath = (String[]) in.readObject();
            int distance = in.readInt();
            double executionTime = in.readDouble();

            // Exibir os resultados no console
            System.out.println("O caminho mais curto é: " + Arrays.toString(shortestPath));
            
            System.out.println("Distância total: " + distance + " Km.");
            
            System.out.printf("Tempo de execução (distribuído): %.6f s%n", executionTime);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro na classe cliente: " + e.getMessage());
        }
    }
}
