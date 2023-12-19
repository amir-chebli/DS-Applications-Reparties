import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Application {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private TextArea chatArea;
    private TextField messageField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            socket = new Socket("localhost", 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Read the welcome message from the server
            String welcomeMessage = in.readLine();

            // Create and configure the JavaFX interface
            primaryStage.setTitle("Chat Client");
            chatArea = new TextArea();
            messageField = new TextField();
            Button sendButton = new Button("Send");
            Button exitButton = new Button("Exit");

            sendButton.setOnAction(event -> sendMessage());
            exitButton.setOnAction(event -> exitChat());

            VBox root = new VBox(chatArea, messageField, sendButton, exitButton);
            Scene scene = new Scene(root, 400, 300);

            primaryStage.setScene(scene);
            primaryStage.show();

            // Saisie du nom par le clavier
            String nom = getUsernameFromDialog();

            // Saisie du mot de passe par le clavier
            String motDePasse = getUsernameFromDialog();

            // Envoie le nom d'utilisateur et le mot de passe au serveur
            out.println(nom + ":" + motDePasse);

            // Lit la réponse du serveur
            String authMessage = in.readLine();
            chatArea.appendText("Serveur: " + authMessage + "\n");

            if (authMessage.startsWith("Bienvenue")) {
                chatArea.appendText("Commencez à discuter. Entrez 'exit' pour quitter.\n");

                // Create a final reference to chatArea for use in lambda expression
                final TextArea finalChatArea = chatArea;

                // Start a new thread to continuously read and display messages
                new Thread(() -> {
                    String serverMessage;
                    try {
                        while ((serverMessage = in.readLine()) != null) {
                            final String messageToShow = "Serveur: " + serverMessage + "\n";
                            Platform.runLater(() -> finalChatArea.appendText(messageToShow));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getUsernameFromDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input");
        dialog.setHeaderText("Enter your input:");
        dialog.setContentText("Input:");
        return dialog.showAndWait().orElse("default");
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.clear();
        }
    }

    private void exitChat() {
        out.println("exit");
        Platform.exit();
    }
}
