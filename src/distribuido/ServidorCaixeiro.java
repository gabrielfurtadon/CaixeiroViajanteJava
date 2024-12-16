package distribuido;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorCaixeiro {
    private final Map<String, Map<String, Integer>> distances;

    public ServidorCaixeiro(Map<String, Map<String, Integer>> distances) {
        this.distances = distances;
    }

    public static <T> void swap(T[] array, int first, int second) {
        T temp = array[first];
        array[first] = array[second];
        array[second] = temp;
    }

    private static <T> void allPermutationsHelper(T[] permutation, List<T[]> permutations, int n) {
        if (n <= 0) {
            permutations.add(Arrays.copyOf(permutation, permutation.length));
            return;
        }
        for (int i = 0; i < n; i++) {
            swap(permutation, i, n - 1);
            allPermutationsHelper(permutation, permutations, n - 1);
            swap(permutation, i, n - 1);
        }
    }

    private static <T> List<T[]> permutations(T[] original) {
        List<T[]> permutations = new ArrayList<>();
        allPermutationsHelper(Arrays.copyOf(original, original.length), permutations, original.length);
        return permutations;
    }

    public int pathDistance(String[] path) {
        int distance = 0;
        for (int i = 0; i < path.length - 1; i++) {
            String from = path[i];
            String to = path[i + 1];

            // Ignorar distâncias entre a mesma cidade
            if (from.equals(to)) {
                continue; // Pula para a próxima iteração
            }

            // Verificar se as cidades existem no mapa de distâncias
            if (!distances.containsKey(from) || !distances.get(from).containsKey(to)) {
                throw new IllegalArgumentException("Distância entre " + from + " e " + to + " não encontrada.");
            }
            distance += distances.get(from).get(to);
        }

        // Adicionar retorno ao ponto inicial, ignorando o caso se for a mesma cidade
        String last = path[path.length - 1];
        String first = path[0];
        if (!last.equals(first)) { // Evita adicionar distância entre a mesma cidade
            if (!distances.containsKey(last) || !distances.get(last).containsKey(first)) {
                throw new IllegalArgumentException("Distância entre " + last + " e " + first + " não encontrada.");
            }
            distance += distances.get(last).get(first);
        }

        return distance;
    }


    public String[] findShortestPath() {
        String[] cities = distances.keySet().toArray(String[]::new);
        List<String[]> paths = permutations(cities);
        String[] shortestPath = null;
        int minDistance = Integer.MAX_VALUE;

        for (String[] path : paths) {
            int distance = pathDistance(path);
            if (distance < minDistance) {
                minDistance = distance;
                shortestPath = path;
            }
        }
        shortestPath = Arrays.copyOf(shortestPath, shortestPath.length + 1);
        shortestPath[shortestPath.length - 1] = shortestPath[0];
        return shortestPath;
    }

    public static void main(String[] args) {
        Map<String, Map<String, Integer>> vtDistances = Map.of(
                "Cornelio", Map.of("Londrina", 67, "Maringa", 162, "Bandeirantes", 37, "Santa Mariana", 18),
                "Londrina", Map.of("Cornelio", 67, "Maringa", 100, "Bandeirantes", 103, "Santa Mariana", 83),
                "Maringa", Map.of("Cornelio", 162, "Londrina", 100, "Bandeirantes", 198, "Santa Mariana", 100),
                "Bandeirantes", Map.of("Cornelio", 37, "Londrina", 103, "Maringa", 198, "Santa Mariana", 20),
                "Santa Mariana", Map.of("Cornelio", 18, "Londrina", 83, "Maringa", 100, "Bandeirantes", 20)
        );

        ServidorCaixeiro server = new ServidorCaixeiro(vtDistances);

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Servidor ok. Aguardando conexões...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

                    System.out.println("Cliente conectado.");

                    long startTime = System.nanoTime();
                    String[] shortestPath = server.findShortestPath();
                    int distance = server.pathDistance(shortestPath);
                    long endTime = System.nanoTime();
                    
                    System.out.println("O caminho mais curto é: " + Arrays.toString(shortestPath));
                    
                    System.out.println("Distância total: " + distance + " Km.");
                    double executionTime = (endTime - startTime) / 1_000_000_000.0;
                    System.out.println("Tempo de execução (distribuído): " + executionTime + " s");

                    out.writeObject(shortestPath);
                    out.writeInt(distance);
                    out.writeDouble(executionTime);
                    out.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Servidor com erro: " + e.getMessage());
        }
    }
}
