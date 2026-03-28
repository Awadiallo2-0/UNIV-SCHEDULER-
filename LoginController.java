package com.univ.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.univ.model.Utilisateur;
import com.univ.dao.UtilisateurDAO;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;

    private Main mainApp;
    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("ADMIN", "GESTIONNAIRE", "ENSEIGNANT", "ETUDIANT");
        roleComboBox.setValue("ADMIN");
        
        // Animation au survol
        loginButton.setOnMouseEntered(e -> 
            loginButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white;"));
        loginButton.setOnMouseExited(e -> 
            loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"));
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String roleStr = roleComboBox.getValue();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs");
            return;
        }

        try {
            Utilisateur.Role role = Utilisateur.Role.valueOf(roleStr);
            Utilisateur user = utilisateurDAO.authenticate(email, password, role);

            if (user != null) {
                statusLabel.setText("Connexion réussie !");
                statusLabel.setStyle("-fx-text-fill: green;");
                
                if (mainApp != null) {
                    mainApp.showMainView(user);
                }
            } else {
                statusLabel.setText("Email ou mot de passe incorrect");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Erreur de connexion");
        }
    }
}