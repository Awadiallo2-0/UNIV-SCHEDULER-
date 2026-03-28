package com.univ.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.univ.model.Utilisateur;

public class Main extends Application {

    private Stage primaryStage;
    private static Main instance;

    @Override
    public void start(Stage stage) throws Exception {
        instance = this;
        this.primaryStage = stage;
        showLoginView();
    }

    public void showLoginView() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        
        LoginController controller = loader.getController();
        controller.setMainApp(this);
        
        Scene scene = new Scene(root);
        
        // Ajout du CSS s'il existe
        try {
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("⚠️ Fichier CSS non trouvé");
        }
        
        primaryStage.setTitle("UNIV SCHEDULER - Connexion");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showMainView(Utilisateur utilisateur) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();
        
        MainController controller = loader.getController();
        controller.setMainApp(this);
        controller.setUtilisateur(utilisateur);
        controller.initView();
        
        Scene scene = new Scene(root);
        
        try {
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("⚠️ Fichier CSS non trouvé");
        }
        
        primaryStage.setTitle("UNIV SCHEDULER - Tableau de bord");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }

    public static Main getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}