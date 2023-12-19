import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Serveur {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private List<String> utilisateursConnectes = new ArrayList<>();

    public Serveur() {
        try {
            this.serverSocket = new ServerSocket(5000);
            System.out.println("Attente d'un nouveau client : ");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nouveau client connecté : " + socket);
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Serveur();
    }

    public boolean authentifierUtilisateur(String nom, String motDePasse) {
        return verifierAuthentification(nom, motDePasse);
    }

    private boolean verifierAuthentification(String nomUtilisateur, String motDePasse) {
        // Charger les informations d'authentification depuis le fichier
        try (BufferedReader br = new BufferedReader(new FileReader("utilisateurs.txt"))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] parties = ligne.split(":");
                if (parties.length == 2 && parties[0].equals(nomUtilisateur) && parties[1].equals(motDePasse)) {
                    return true;  // Authentification réussie
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;  // Authentification échouée
    }

    public void diffuserMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(sender.getNom() + ": " + message);
            }
        }
    }

    public void supprimerClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client déconnecté : " + client.getNom());
    }

    // Add this method
    public void ajouterUtilisateurConnecte(String nomUtilisateur) {
        utilisateursConnectes.add(nomUtilisateur);
    }
}
