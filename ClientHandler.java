import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Serveur serveur;
    private String nom;
    private String motDePasse;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, Serveur serveur) {
        this.socket = socket;
        this.serveur = serveur;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNom() {
        return nom;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    @Override
    public void run() {
        try {
            out.println("Bienvenue! Veuillez saisir votre nom d'utilisateur et mot de passe séparés par ':' : ");
            String credentials = in.readLine();
    
            // Vérification de l'authentification
            String[] parts = credentials.split(":");
            if (parts.length == 2 && serveur.authentifierUtilisateur(parts[0], parts[1]) && !parts[0].trim().isEmpty()) {
                this.nom = parts[0]; // Set the username
                serveur.ajouterUtilisateurConnecte(parts[0]);
                out.println("Bienvenue, " + parts[0] + "! Commencez à discuter.");
            } else {
                out.println("Nom d'utilisateur ou mot de passe incorrect. La connexion a échoué.");
                socket.close();
                return;
            }
    
            String message;
            while ((message = in.readLine()) != null) {
                serveur.diffuserMessage(message, this);
            }
        } catch (IOException e) {
            // Handle disconnection
            System.out.println("Confirmation de la déconnexion du client: " + nom);
        } finally {
            // Cleanup and remove client from the server
            serveur.supprimerClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }        
}
