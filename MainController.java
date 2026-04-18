package com.univ.ui;

import javafx.animation.FadeTransition;
import java.net.URL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import com.univ.model.*;
import com.univ.dao.*;
import com.univ.service.*;
import com.univ.utils.ExportUtils;
import com.univ.utils.PdfUtils;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.net.URL;
public class MainController {

    @FXML private BorderPane rootPane;
    @FXML private VBox menuVBox;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private StackPane contentArea;
    @FXML private ImageView logoImageView;
    
    // Boutons du menu
    @FXML private Button dashboardBtn;
    @FXML private Button batimentBtn;
    @FXML private Button salleBtn;
    @FXML private Button equipementBtn;
    @FXML private Button coursBtn;
    @FXML private Button emploiBtn;
    @FXML private Button rechercheBtn;
    @FXML private Button rapportBtn;
    @FXML private Button logoutBtn;
    @FXML private Button themeToggleBtn;

    private Main mainApp;
    private Utilisateur utilisateur;
    
    // DAOs
    private BatimentDAO batimentDAO = new BatimentDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private EquipementDAO equipementDAO = new EquipementDAO();
    private CoursDAO coursDAO = new CoursDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    
    // Services
    private PlanningService planningService = new PlanningService();
    private ConflitDetector conflitDetector = new ConflitDetector();
    private NotificationService notificationService = new NotificationService();
    private StatistiqueService statistiqueService = new StatistiqueService();

    private boolean isDarkTheme = false;
    private static final String LIGHT_THEME = "/css/style.css";
    private static final String DARK_THEME = "/css/dark.css";

    @FXML
    public void initialize() {
        setupMenuAnimations();
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
        
        // Charger le logo si disponible
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/imageuidt.jpeg"));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            // Image en ligne par défaut
            Image logo = new Image("https://img.icons8.com/fluency/96/000000/university.png");
            logoImageView.setImage(logo);
        }

        Object themeToggleBtn = null;
		// Configuration du bouton de thème
        if (themeToggleBtn != null) {
            ((ButtonBase) themeToggleBtn).setOnAction(e -> toggleTheme());
            ((Labeled) themeToggleBtn).setText("🌙"); // lune pour thème sombre, soleil pour clair
        }
        
        // Appliquer le thème par défaut (clair)
        applyTheme(false);
    }

    /**
     * Animation des boutons du menu : effet de surbrillance sans disparition.
     * Les boutons restent toujours visibles avec un fond sombre permanent.
     */
    private void setupMenuAnimations() {
        // Style de base pour tous les boutons du menu
        String baseStyle = "-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 14px; " +
                           "-fx-alignment: CENTER-LEFT; -fx-padding: 10; -fx-background-radius: 5;";
        
        Button[] menuButtons = {dashboardBtn, batimentBtn, salleBtn, equipementBtn, 
                                coursBtn, emploiBtn, rechercheBtn, rapportBtn, logoutBtn};
        
        for (Button btn : menuButtons) {
            btn.setStyle(baseStyle);
            // Effet au survol : changement de couleur de fond (plus clair) mais reste visible
            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-size: 14px; " +
                             "-fx-alignment: CENTER-LEFT; -fx-padding: 10; -fx-background-radius: 5; -fx-cursor: hand;");
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle(baseStyle);
            });
        }
    }

    /**
     * Change le thème (clair/sombre) en remplaçant la feuille de style principale.
     */
    @FXML
    public void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        applyTheme(isDarkTheme);
        if (themeToggleBtn != null) {
            themeToggleBtn.setText(isDarkTheme ? "☀️" : "🌙");
        }
    }

    private void applyTheme(boolean dark) {
        Scene scene = rootPane.getScene();
        if (scene == null) return;
        scene.getStylesheets().clear();
        String themePath = dark ? "/css/dark.css" : "/css/style.css";
        URL themeUrl = getClass().getResource(themePath);
        if (themeUrl != null) {
            scene.getStylesheets().add(themeUrl.toExternalForm());
        }
        // Couleurs de fond principales
        String bgColor = dark ? "#121212" : "#ecf0f1";
        rootPane.setStyle("-fx-background-color: " + bgColor + ";");
        if (contentArea != null) {
            contentArea.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 20;");
        }
        updateMenuColors(dark);
    }

    private void updateMenuColors(boolean dark) {
        String bgColor = dark ? "#1e2a36" : "#2c3e50";
        String textColor = "white";
        String baseStyle = String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-font-size: 14px; " +
                                         "-fx-alignment: CENTER-LEFT; -fx-padding: 10; -fx-background-radius: 5;", bgColor, textColor);
        Button[] menuButtons = {dashboardBtn, batimentBtn, salleBtn, equipementBtn, 
                                coursBtn, emploiBtn, rechercheBtn, rapportBtn, logoutBtn};
        for (Button btn : menuButtons) {
            btn.setStyle(baseStyle);
        }
        // Changer la couleur du panneau latéral
        menuVBox.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 20;");
    }

    public void initView() {
        if (utilisateur != null) {
            userLabel.setText(utilisateur.getPrenom() + " " + utilisateur.getNom());
            roleLabel.setText(utilisateur.getRole().toString());
        }
        showDashboard();
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void showDashboard() {
        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(20));
        dashboard.setStyle("-fx-background-color: #f5f5f5;");

        HBox statsBox = createStatsCards();
        HBox chartsBox = createCharts();
        VBox recentReservations = createRecentReservations();
        
        dashboard.getChildren().addAll(statsBox, chartsBox, recentReservations);
        setContent(dashboard);
    }
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos");
        alert.setHeaderText(null);
        alert.setContentText("UNIV-SCHEDULER v1.0\n\nApplication de gestion des salles et emplois du temps.\nDéveloppé avec JavaFX et MySQL.");
        alert.showAndWait();
    }

    @FXML
    private void showBatiments() {
        VBox view = createBatimentsView();
        setContent(view);
    }

    @FXML
    private void showSalles() {
        VBox view = createSallesView();
        setContent(view);
    }

    @FXML
    private void showEquipements() {
        VBox view = createEquipementsView();
        setContent(view);
    }

    @FXML
    private void showCours() {
        VBox view = createCoursView();
        setContent(view);
    }

    @FXML
    private void showEmploiDuTemps() {
        VBox view = createEmploiDuTempsView();
        setContent(view);
    }

    @FXML
    private void showRecherche() {
        VBox view = createRechercheView();
        setContent(view);
    }

    @FXML
    private void showRapports() {
        VBox view = createRapportsView();
        setContent(view);
    }

    @FXML
    private void handleLogout() {
        try {
            mainApp.showLoginView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    // ==================== MÉTHODES DE CRÉATION DE VUES ====================

    private VBox createBatimentsView() {
        VBox view = new VBox(10);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: white;");

        Label title = new Label("Gestion des Bâtiments");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10, 0, 10, 0));
        
        Button addBtn = createStyledButton("Ajouter", "➕", "#27ae60");
        Button editBtn = createStyledButton("Modifier", "✏️", "#f39c12");
        Button deleteBtn = createStyledButton("Supprimer", "🗑️", "#e74c3c");
        Button refreshBtn = createStyledButton("Actualiser", "🔄", "#3498db");
        
        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn);

        TableView<Batiment> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Batiment, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Batiment, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        
        TableColumn<Batiment, String> localisationCol = new TableColumn<>("Localisation");
        localisationCol.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        
        TableColumn<Batiment, Integer> etagesCol = new TableColumn<>("Étages");
        etagesCol.setCellValueFactory(new PropertyValueFactory<>("nombreEtages"));
        
        table.getColumns().addAll(idCol, nomCol, localisationCol, etagesCol);
        
        ObservableList<Batiment> data = FXCollections.observableArrayList(batimentDAO.findAll());
        table.setItems(data);

        addBtn.setOnAction(e -> showAddBatimentDialog1(data));
        editBtn.setOnAction(e -> {
            Batiment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showEditBatimentDialog(selected, data);
        });
        deleteBtn.setOnAction(e -> {
            Batiment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showDeleteBatimentDialog(selected, data);
        });
        refreshBtn.setOnAction(e -> data.setAll(batimentDAO.findAll()));

        view.getChildren().addAll(title, toolbar, table);
        return view;
    }

    private VBox createSallesView() {
        VBox view = new VBox(10);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: white;");

        Label title = new Label("Gestion des Salles");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10, 0, 10, 0));
        
        Button addBtn = createStyledButton("Ajouter", "➕", "#27ae60");
        Button editBtn = createStyledButton("Modifier", "✏️", "#f39c12");
        Button deleteBtn = createStyledButton("Supprimer", "🗑️", "#e74c3c");
        Button refreshBtn = createStyledButton("Actualiser", "🔄", "#3498db");
        
        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn);

        TableView<Salle> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Salle, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Salle, String> numeroCol = new TableColumn<>("Numéro");
        numeroCol.setCellValueFactory(new PropertyValueFactory<>("numero"));
        
        TableColumn<Salle, Integer> capaciteCol = new TableColumn<>("Capacité");
        capaciteCol.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        
        TableColumn<Salle, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType().toString()));
        
        TableColumn<Salle, String> batimentCol = new TableColumn<>("Bâtiment");
        batimentCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getBatiment() != null ? 
                cellData.getValue().getBatiment().getNom() : ""));
        
        TableColumn<Salle, Integer> etageCol = new TableColumn<>("Étage");
        etageCol.setCellValueFactory(new PropertyValueFactory<>("etage"));
        
        table.getColumns().addAll(idCol, numeroCol, capaciteCol, typeCol, batimentCol, etageCol);
        
        ObservableList<Salle> data = FXCollections.observableArrayList(salleDAO.findAll());
        table.setItems(data);

        addBtn.setOnAction(e -> showAddSalleDialog(data));
        editBtn.setOnAction(e -> {
            Salle selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showEditSalleDialog(selected, data);
        });
        deleteBtn.setOnAction(e -> {
            Salle selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showDeleteSalleDialog(selected, data);
        });
        refreshBtn.setOnAction(e -> data.setAll(salleDAO.findAll()));

        view.getChildren().addAll(title, toolbar, table);
        return view;
    }

    private VBox createEquipementsView() {
        VBox view = new VBox(10);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: white;");

        Label title = new Label("Gestion des Équipements");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10, 0, 10, 0));
        
        Button addBtn = createStyledButton("Ajouter", "➕", "#27ae60");
        Button editBtn = createStyledButton("Modifier", "✏️", "#f39c12");
        Button deleteBtn = createStyledButton("Supprimer", "🗑️", "#e74c3c");
        Button refreshBtn = createStyledButton("Actualiser", "🔄", "#3498db");
        
        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn);

        TableView<Equipement> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Equipement, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Equipement, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        
        TableColumn<Equipement, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        table.getColumns().addAll(idCol, nomCol, descriptionCol);
        
        ObservableList<Equipement> data = FXCollections.observableArrayList(equipementDAO.findAll());
        table.setItems(data);

        addBtn.setOnAction(e -> showAddEquipementDialog(data));
        editBtn.setOnAction(e -> {
            Equipement selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showEditEquipementDialog(selected, data);
        });
        deleteBtn.setOnAction(e -> {
            Equipement selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showDeleteEquipementDialog(selected, data);
        });
        refreshBtn.setOnAction(e -> data.setAll(equipementDAO.findAll()));

        view.getChildren().addAll(title, toolbar, table);
        return view;
    }

    private VBox createCoursView() {
        VBox view = new VBox(10);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: white;");

        Label title = new Label("Gestion des Cours");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10, 0, 10, 0));
        
        Button addBtn = createStyledButton("Ajouter", "➕", "#27ae60");
        Button editBtn = createStyledButton("Modifier", "✏️", "#f39c12");
        Button deleteBtn = createStyledButton("Supprimer", "🗑️", "#e74c3c");
        Button refreshBtn = createStyledButton("Actualiser", "🔄", "#3498db");
        
        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn, refreshBtn);

        TableView<Cours> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Cours, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Cours, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        
        TableColumn<Cours, String> matiereCol = new TableColumn<>("Matière");
        matiereCol.setCellValueFactory(new PropertyValueFactory<>("matiere"));
        
        TableColumn<Cours, String> enseignantCol = new TableColumn<>("Enseignant");
        enseignantCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getEnseignant() != null ? 
                cellData.getValue().getEnseignant().getPrenom() + " " + 
                cellData.getValue().getEnseignant().getNom() : ""));
        
        TableColumn<Cours, String> classeCol = new TableColumn<>("Classe");
        classeCol.setCellValueFactory(new PropertyValueFactory<>("classe"));
        
        TableColumn<Cours, String> groupeCol = new TableColumn<>("Groupe");
        groupeCol.setCellValueFactory(new PropertyValueFactory<>("groupe"));
        
        table.getColumns().addAll(idCol, nomCol, matiereCol, enseignantCol, classeCol, groupeCol);
        
        ObservableList<Cours> data = FXCollections.observableArrayList(coursDAO.findAll());
        table.setItems(data);

        addBtn.setOnAction(e -> showAddCoursDialog(data));
        editBtn.setOnAction(e -> {
            Cours selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showEditCoursDialog(selected, data);
        });
        deleteBtn.setOnAction(e -> {
            Cours selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showDeleteCoursDialog(selected, data);
        });
        refreshBtn.setOnAction(e -> data.setAll(coursDAO.findAll()));

        view.getChildren().addAll(title, toolbar, table);
        return view;
    }

    private VBox createRechercheView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: white;");

        Label title = new Label("Recherche de Salles Disponibles");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane searchForm = new GridPane();
        searchForm.setHgap(15);
        searchForm.setVgap(15);
        searchForm.setPadding(new Insets(20));
        searchForm.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(200);
        
        ComboBox<String> heureDebutCombo = new ComboBox<>();
        heureDebutCombo.getItems().addAll("08:00", "09:00", "10:00", "11:00", "12:00", 
                                         "13:00", "14:00", "15:00", "16:00", "17:00", "18:00");
        heureDebutCombo.setValue("08:00");
        
        ComboBox<String> heureFinCombo = new ComboBox<>();
        heureFinCombo.getItems().addAll("09:00", "10:00", "11:00", "12:00", "13:00", 
                                       "14:00", "15:00", "16:00", "17:00", "18:00", "19:00");
        heureFinCombo.setValue("10:00");
        
        TextField capaciteField = new TextField();
        capaciteField.setPromptText("Capacité minimale");
        
        ComboBox<Salle.TypeSalle> typeCombo = new ComboBox<>();
        typeCombo.getItems().setAll(Salle.TypeSalle.values());
        typeCombo.setPromptText("Type de salle");
        
        VBox equipBox = new VBox(5);
        equipBox.setPadding(new Insets(5));
        CheckBox videoCheck = new CheckBox("Vidéoprojecteur");
        CheckBox tableauCheck = new CheckBox("Tableau interactif");
        CheckBox climCheck = new CheckBox("Climatisation");
        equipBox.getChildren().addAll(videoCheck, tableauCheck, climCheck);

        searchForm.add(new Label("Date:"), 0, 0);
        searchForm.add(datePicker, 1, 0);
        searchForm.add(new Label("Heure début:"), 0, 1);
        searchForm.add(heureDebutCombo, 1, 1);
        searchForm.add(new Label("Heure fin:"), 0, 2);
        searchForm.add(heureFinCombo, 1, 2);
        searchForm.add(new Label("Capacité min:"), 0, 3);
        searchForm.add(capaciteField, 1, 3);
        searchForm.add(new Label("Type:"), 0, 4);
        searchForm.add(typeCombo, 1, 4);
        searchForm.add(new Label("Équipements:"), 0, 5);
        searchForm.add(equipBox, 1, 5);

        Button searchBtn = createStyledButton("Rechercher", "🔍", "#3498db");
        searchBtn.setPrefWidth(200);
        searchForm.add(searchBtn, 1, 6);

        VBox resultsBox = new VBox(10);
        resultsBox.setPadding(new Insets(10));
        resultsBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");
        
        Label resultsTitle = new Label("Résultats de la recherche");
        resultsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> resultsList = new ListView<>();
        resultsList.setPrefHeight(300);

        resultsBox.getChildren().addAll(resultsTitle, resultsList);

        searchBtn.setOnAction(e -> {
            try {
                LocalDate date = datePicker.getValue();
                LocalTime debut = LocalTime.parse(heureDebutCombo.getValue());
                LocalTime fin = LocalTime.parse(heureFinCombo.getValue());
                Integer capacite = capaciteField.getText().isEmpty() ? null : 
                                  Integer.parseInt(capaciteField.getText());
                
                List<Salle> salles = planningService.rechercherSallesDisponibles(date, debut, fin, capacite);
                
                if (typeCombo.getValue() != null) {
                    salles = salles.stream()
                        .filter(s -> s.getType() == typeCombo.getValue())
                        .collect(Collectors.toList());
                }
                
                if (videoCheck.isSelected()) {
                    salles = salles.stream()
                        .filter(s -> s.aEquipement("Vidéoprojecteur"))
                        .collect(Collectors.toList());
                }
                if (tableauCheck.isSelected()) {
                    salles = salles.stream()
                        .filter(s -> s.aEquipement("Tableau interactif"))
                        .collect(Collectors.toList());
                }
                if (climCheck.isSelected()) {
                    salles = salles.stream()
                        .filter(s -> s.aEquipement("Climatisation"))
                        .collect(Collectors.toList());
                }
                
                resultsList.getItems().clear();
                if (salles.isEmpty()) {
                    resultsList.getItems().add("Aucune salle disponible pour ces critères.");
                } else {
                    for (Salle s : salles) {
                        String info = String.format("%s - Salle %s | Cap: %d | %s | Équipements: %s",
                            s.getBatiment().getNom(),
                            s.getNumero(),
                            s.getCapacite(),
                            s.getType(),
                            s.getEquipements().keySet().stream()
                                .map(Equipement::getNom)
                                .collect(Collectors.joining(", ")));
                        resultsList.getItems().add(info);
                    }
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        });

        view.getChildren().addAll(title, searchForm, resultsBox);
        return view;
    }
    private void showAddBatimentDialog1(ObservableList<Batiment> data) {
        Dialog<Batiment> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un bâtiment");
        dialog.setHeaderText("Nouveau bâtiment");

        ButtonType saveButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        TextField localisationField = new TextField();
        localisationField.setPromptText("Localisation");
        TextField etagesField = new TextField();
        etagesField.setPromptText("Nombre d'étages");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Localisation:"), 0, 1);
        grid.add(localisationField, 1, 1);
        grid.add(new Label("Étages:"), 0, 2);
        grid.add(etagesField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Batiment b = new Batiment();
                b.setNom(nomField.getText());
                b.setLocalisation(localisationField.getText());
                try {
                    b.setNombreEtages(Integer.parseInt(etagesField.getText()));
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Le nombre d'étages doit être un entier.");
                    return null;
                }
                return b;
            }
            return null;
        });

        Optional<Batiment> result = dialog.showAndWait();
        result.ifPresent(batiment -> {
            if (batimentDAO.create(batiment)) {
                data.setAll(batimentDAO.findAll());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Bâtiment ajouté !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter le bâtiment.");
            }
        });
    }

    private void showEditBatimentDialog(Batiment batiment, ObservableList<Batiment> data) {
        Dialog<Batiment> dialog = new Dialog<>();
        dialog.setTitle("Modifier un bâtiment");
        dialog.setHeaderText("Modification du bâtiment " + batiment.getNom());

        ButtonType saveButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField(batiment.getNom());
        TextField localisationField = new TextField(batiment.getLocalisation());
        TextField etagesField = new TextField(String.valueOf(batiment.getNombreEtages()));

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Localisation:"), 0, 1);
        grid.add(localisationField, 1, 1);
        grid.add(new Label("Étages:"), 0, 2);
        grid.add(etagesField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                batiment.setNom(nomField.getText());
                batiment.setLocalisation(localisationField.getText());
                try {
                    batiment.setNombreEtages(Integer.parseInt(etagesField.getText()));
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Le nombre d'étages doit être un entier.");
                    return null;
                }
                return batiment;
            }
            return null;
        });

        Optional<Batiment> result = dialog.showAndWait();
        result.ifPresent(b -> {
            if (batimentDAO.update(b)) {
                data.setAll(batimentDAO.findAll());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Bâtiment modifié !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier le bâtiment.");
            }
        });
    }

    private void showDeleteBatimentDialog(Batiment batiment, ObservableList<Batiment> data) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer le bâtiment " + batiment.getNom() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (batimentDAO.delete(batiment.getId())) {
                    data.setAll(batimentDAO.findAll());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Bâtiment supprimé !");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le bâtiment.");
                }
            }
        });
    }
    private void showAddSalleDialog(ObservableList<Salle> data) {
        Dialog<Salle> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une salle");
        dialog.setHeaderText("Nouvelle salle");

        ButtonType saveButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField numeroField = new TextField();
        numeroField.setPromptText("Numéro");
        TextField capaciteField = new TextField();
        capaciteField.setPromptText("Capacité");
        ComboBox<Salle.TypeSalle> typeCombo = new ComboBox<>();
        typeCombo.getItems().setAll(Salle.TypeSalle.values());
        typeCombo.setValue(Salle.TypeSalle.TD);
        ComboBox<Batiment> batimentCombo = new ComboBox<>();
        batimentCombo.getItems().setAll(batimentDAO.findAll());
        TextField etageField = new TextField();
        etageField.setPromptText("Étage");

        grid.add(new Label("Numéro:"), 0, 0);
        grid.add(numeroField, 1, 0);
        grid.add(new Label("Capacité:"), 0, 1);
        grid.add(capaciteField, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeCombo, 1, 2);
        grid.add(new Label("Bâtiment:"), 0, 3);
        grid.add(batimentCombo, 1, 3);
        grid.add(new Label("Étage:"), 0, 4);
        grid.add(etageField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Salle s = new Salle();
                s.setNumero(numeroField.getText());
                try {
                    s.setCapacite(Integer.parseInt(capaciteField.getText()));
                    s.setEtage(Integer.parseInt(etageField.getText()));
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Capacité et étage doivent être des entiers.");
                    return null;
                }
                s.setType(typeCombo.getValue());
                s.setBatiment(batimentCombo.getValue());
                return s;
            }
            return null;
        });

        Optional<Salle> result = dialog.showAndWait();
        result.ifPresent(salle -> {
            if (salleDAO.create(salle)) {
                data.setAll(salleDAO.findAll());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Salle ajoutée !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter la salle.");
            }
        });
    }

    private void showEditSalleDialog(Salle salle, ObservableList<Salle> data) {
        Dialog<Salle> dialog = new Dialog<>();
        dialog.setTitle("Modifier une salle");
        dialog.setHeaderText("Modification de la salle " + salle.getNumero());

        ButtonType saveButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField numeroField = new TextField(salle.getNumero());
        TextField capaciteField = new TextField(String.valueOf(salle.getCapacite()));
        ComboBox<Salle.TypeSalle> typeCombo = new ComboBox<>();
        typeCombo.getItems().setAll(Salle.TypeSalle.values());
        typeCombo.setValue(salle.getType());
        ComboBox<Batiment> batimentCombo = new ComboBox<>();
        batimentCombo.getItems().setAll(batimentDAO.findAll());
        batimentCombo.setValue(salle.getBatiment());
        TextField etageField = new TextField(String.valueOf(salle.getEtage()));

        grid.add(new Label("Numéro:"), 0, 0);
        grid.add(numeroField, 1, 0);
        grid.add(new Label("Capacité:"), 0, 1);
        grid.add(capaciteField, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeCombo, 1, 2);
        grid.add(new Label("Bâtiment:"), 0, 3);
        grid.add(batimentCombo, 1, 3);
        grid.add(new Label("Étage:"), 0, 4);
        grid.add(etageField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                salle.setNumero(numeroField.getText());
                try {
                    salle.setCapacite(Integer.parseInt(capaciteField.getText()));
                    salle.setEtage(Integer.parseInt(etageField.getText()));
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Capacité et étage doivent être des entiers.");
                    return null;
                }
                salle.setType(typeCombo.getValue());
                salle.setBatiment(batimentCombo.getValue());
                return salle;
            }
            return null;
        });

        Optional<Salle> result = dialog.showAndWait();
        result.ifPresent(s -> {
            if (salleDAO.update(s)) {
                data.setAll(salleDAO.findAll());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Salle modifiée !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier la salle.");
            }
        });
    }

    private void showDeleteSalleDialog(Salle salle, ObservableList<Salle> data) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer la salle " + salle.getNumero() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (salleDAO.delete(salle.getId())) {
                    data.setAll(salleDAO.findAll());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Salle supprimée !");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la salle.");
                }
            }
        });
    }
    private void showAddEquipementDialog(ObservableList<Equipement> data) {
        Dialog<Equipement> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un équipement");
        dialog.setHeaderText("Nouvel équipement");

        ButtonType saveButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Equipement e = new Equipement();
                e.setNom(nomField.getText());
                e.setDescription(descriptionField.getText());
                return e;
            }
            return null;
        });

        Optional<Equipement> result = dialog.showAndWait();
        result.ifPresent(equipement -> {
            if (equipementDAO.create(equipement)) {
                data.setAll(equipementDAO.findAll());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Équipement ajouté !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter l'équipement.");
            }
        });
    }

    private void showEditEquipementDialog(Equipement equipement, ObservableList<Equipement> data) {
        Dialog<Equipement> dialog = new Dialog<>();
        dialog.setTitle("Modifier un équipement");
        dialog.setHeaderText("Modification de l'équipement " + equipement.getNom());

        ButtonType saveButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField(equipement.getNom());
        TextField descriptionField = new TextField(equipement.getDescription());

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                equipement.setNom(nomField.getText());
                equipement.setDescription(descriptionField.getText());
                return equipement;
            }
            return null;
        });

        Optional<Equipement> result = dialog.showAndWait();
        result.ifPresent(e -> {
            if (equipementDAO.update(e)) {
                data.setAll(equipementDAO.findAll());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Équipement modifié !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier l'équipement.");
            }
        });
    }

    private void showDeleteEquipementDialog(Equipement equipement, ObservableList<Equipement> data) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l'équipement " + equipement.getNom() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (equipementDAO.delete(equipement.getId())) {
                    data.setAll(equipementDAO.findAll());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Équipement supprimé !");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'équipement.");
                }
            }
        });
    }
    private void showAddCoursDialog(ObservableList<Cours> data) {
        Dialog<Cours> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un cours");
        dialog.setHeaderText("Nouveau cours");

        ButtonType saveButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom du cours");
        TextField matiereField = new TextField();
        matiereField.setPromptText("Matière");
        ComboBox<Utilisateur> enseignantCombo = new ComboBox<>();
        enseignantCombo.getItems().setAll(
            utilisateurDAO.findAll().stream()
                .filter(u -> u.getRole() == Utilisateur.Role.ENSEIGNANT)
                .collect(Collectors.toList())
        );
        TextField classeField = new TextField();
        classeField.setPromptText("Classe");
        TextField groupeField = new TextField();
        groupeField.setPromptText("Groupe");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Matière:"), 0, 1);
        grid.add(matiereField, 1, 1);
        grid.add(new Label("Enseignant:"), 0, 2);
        grid.add(enseignantCombo, 1, 2);
        grid.add(new Label("Classe:"), 0, 3);
        grid.add(classeField, 1, 3);
        grid.add(new Label("Groupe:"), 0, 4);
        grid.add(groupeField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Cours c = new Cours();
                c.setNom(nomField.getText());
                c.setMatiere(matiereField.getText());
                c.setEnseignant(enseignantCombo.getValue());
                c.setClasse(classeField.getText());
                c.setGroupe(groupeField.getText());
                return c;
            }
            return null;
        });

        Optional<Cours> result = dialog.showAndWait();
        result.ifPresent(cours -> {
            if (coursDAO.create(cours)) {
                data.setAll(coursDAO.findAll());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours ajouté !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter le cours.");
            }
        });
    }

    private void showEditCoursDialog(Cours cours, ObservableList<Cours> data) {
        Dialog<Cours> dialog = new Dialog<>();
        dialog.setTitle("Modifier un cours");
        dialog.setHeaderText("Modification du cours " + cours.getNom());

        ButtonType saveButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField(cours.getNom());
        TextField matiereField = new TextField(cours.getMatiere());
        ComboBox<Utilisateur> enseignantCombo = new ComboBox<>();
        enseignantCombo.getItems().setAll(
            utilisateurDAO.findAll().stream()
                .filter(u -> u.getRole() == Utilisateur.Role.ENSEIGNANT)
                .collect(Collectors.toList())
        );
        enseignantCombo.setValue(cours.getEnseignant());
        TextField classeField = new TextField(cours.getClasse());
        TextField groupeField = new TextField(cours.getGroupe());

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Matière:"), 0, 1);
        grid.add(matiereField, 1, 1);
        grid.add(new Label("Enseignant:"), 0, 2);
        grid.add(enseignantCombo, 1, 2);
        grid.add(new Label("Classe:"), 0, 3);
        grid.add(classeField, 1, 3);
        grid.add(new Label("Groupe:"), 0, 4);
        grid.add(groupeField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                cours.setNom(nomField.getText());
                cours.setMatiere(matiereField.getText());
                cours.setEnseignant(enseignantCombo.getValue());
                cours.setClasse(classeField.getText());
                cours.setGroupe(groupeField.getText());
                return cours;
            }
            return null;
        });

        Optional<Cours> result = dialog.showAndWait();
        result.ifPresent(c -> {
            if (coursDAO.update(c)) {
                data.setAll(coursDAO.findAll());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours modifié !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier le cours.");
            }
        });
    }

    private void showDeleteCoursDialog(Cours cours, ObservableList<Cours> data) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer le cours " + cours.getNom() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (coursDAO.delete(cours.getId())) {
                    data.setAll(coursDAO.findAll());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours supprimé !");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le cours.");
                }
            }
        });
    }

    // ==================== EMPLOI DU TEMPS ====================

    private VBox createEmploiDuTempsView() {
        VBox view = new VBox(10);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: white;");

        Label title = new Label("Emploi du Temps");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10, 0, 10, 0));
        controls.setAlignment(Pos.CENTER_LEFT);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-font-size: 14px;");
        
        ToggleGroup viewGroup = new ToggleGroup();
        RadioButton jourBtn = new RadioButton("Jour");
        jourBtn.setToggleGroup(viewGroup);
        jourBtn.setSelected(true);
        RadioButton semaineBtn = new RadioButton("Semaine");
        semaineBtn.setToggleGroup(viewGroup);
        RadioButton moisBtn = new RadioButton("Mois");
        moisBtn.setToggleGroup(viewGroup);
        
        Button addBtn = createStyledButton("Ajouter une réservation", "➕", "#27ae60");
        
        controls.getChildren().addAll(new Label("Date:"), datePicker, 
                                      jourBtn, semaineBtn, moisBtn, addBtn);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        GridPane emploiGrid = createEmploiDuTempsGrid(datePicker.getValue());
        scrollPane.setContent(emploiGrid);

        datePicker.setOnAction(e -> {
            GridPane newGrid = createEmploiDuTempsGrid(datePicker.getValue());
            scrollPane.setContent(newGrid);
        });

        addBtn.setOnAction(e -> showAddReservationDialog(null, datePicker.getValue()));

        view.getChildren().addAll(title, controls, scrollPane);
        return view;
    }

    /**
     * Grille emploi du temps : jours horizontaux, heures verticales (8h-19h)
     * Chaque cellule affiche les cours (avec salle) ayant lieu à ce créneau.
     */
    private GridPane createEmploiDuTempsGrid(LocalDate date) {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #f0f0f0;");

        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
        LocalDate startOfWeek = date.minusDays(date.getDayOfWeek().getValue() - 1);
        
        for (int i = 0; i < jours.length; i++) {
            Label jourLabel = new Label(jours[i]);
            jourLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #3498db; -fx-text-fill: white; -fx-alignment: CENTER;");
            jourLabel.setMinWidth(150);
            jourLabel.setAlignment(Pos.CENTER);
            grid.add(jourLabel, i + 1, 0);
        }

        // Heures (colonne verticale) de 8h à 19h
        String[] heures = {"08:00", "09:00", "10:00", "11:00", "12:00", 
                           "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00"};
        
        for (int i = 0; i < heures.length; i++) {
            Label heureLabel = new Label(heures[i]);
            heureLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            heureLabel.setMinWidth(60);
            heureLabel.setAlignment(Pos.CENTER_RIGHT);
            grid.add(heureLabel, 0, i + 1);
        }

        // Remplir les cellules pour chaque jour et chaque heure
        for (int jourIdx = 0; jourIdx < jours.length; jourIdx++) {
            LocalDate currentDate = startOfWeek.plusDays(jourIdx);
            for (int heureIdx = 0; heureIdx < heures.length; heureIdx++) {
                String heureStr = heures[heureIdx];
                LocalTime debut = LocalTime.parse(heureStr);
                LocalTime fin = debut.plusHours(1);
                
                // Chercher toutes les réservations (non annulées) pour ce jour et ce créneau
                List<Reservation> reservations = reservationDAO.findAll().stream()
                        .filter(r -> r.getStatut() != Reservation.Statut.ANNULE)
                        .filter(r -> r.getDate().equals(currentDate))
                        .filter(r -> !r.getHeureDebut().isAfter(debut) && !r.getHeureFin().isBefore(fin))
                        .collect(Collectors.toList());
                
                StackPane cell = new StackPane();
                cell.setMinSize(150, 40);
                cell.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1;");
                
                if (!reservations.isEmpty()) {
                    // Construire un texte avec tous les cours de ce créneau
                    VBox content = new VBox(2);
                    content.setAlignment(Pos.CENTER_LEFT);
                    content.setPadding(new Insets(2));
                    
                    for (Reservation r : reservations) {
                        String texte = (r.getCours() != null ? r.getCours().getNom() : r.getType().toString()) 
                                     + " (" + r.getSalle().getNumero() + ")";
                        Label label = new Label(texte);
                        label.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");
                        label.setWrapText(true);
                        
                        // Couleur selon le type
                        String color;
                        switch (r.getType()) {
                            case COURS: color = "#3498db"; break;
                            case REUNION: color = "#f39c12"; break;
                            case SOUTENANCE: color = "#9b59b6"; break;
                            default: color = "#2ecc71";
                        }
                        label.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: " + color + "; -fx-padding: 2;");
                        content.getChildren().add(label);
                    }
                    
                    cell.setStyle("-fx-background-color: rgba(0,0,0,0.1); -fx-background-radius: 3; -fx-border-color: #ddd; -fx-border-width: 1;");
                    cell.getChildren().add(content);
                    
                    // Double-clic : on passe la première réservation (ou on pourrait gérer plusieurs)
                    cell.setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                            showReservationOptionsDialog(reservations.get(0));
                        }
                    });
                } else {
                    // Cellule vide - on ajoute un label "libre"
                    Label libreLabel = new Label("libre");
                    libreLabel.setStyle("-fx-text-fill: #aaa; -fx-font-style: italic; -fx-font-size: 10px;");
                    cell.getChildren().add(libreLabel);
                    
                    cell.setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                            showAddReservationDialog(null, currentDate);
                        }
                    });
                }
                
                grid.add(cell, jourIdx + 1, heureIdx + 1);
            }
        }

        return grid;
    }

    // ==================== RAPPORTS ET STATISTIQUES ====================
    private VBox createRapportsView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: white;");

        Label title = new Label("Rapports et Statistiques");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox periodeBox = new HBox(10);
        periodeBox.setAlignment(Pos.CENTER_LEFT);
        
        DatePicker debutPicker = new DatePicker(LocalDate.now().minusWeeks(1));
        DatePicker finPicker = new DatePicker(LocalDate.now());
        
        Button rafraichirBtn = createStyledButton("Rafraîchir", "🔄", "#3498db");
        rafraichirBtn.setPrefWidth(120);
        
        periodeBox.getChildren().addAll(
            new Label("Du:"), debutPicker,
            new Label("Au:"), finPicker,
            rafraichirBtn
        );

        // Conteneur pour les graphiques (sera mis à jour)
        VBox chartsContainer = new VBox(20);
        chartsContainer.setPadding(new Insets(10));

        // Mettre le contenu des graphiques dans un ScrollPane pour éviter les coupures
        ScrollPane scrollPane = new ScrollPane(chartsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Fonction de mise à jour
        Runnable updateCharts = () -> {
            LocalDate debut = debutPicker.getValue();
            LocalDate fin = finPicker.getValue();
            if (debut == null || fin == null) return;
            if (debut.isAfter(fin)) {
                showAlert(Alert.AlertType.WARNING, "Attention", "La date de début doit être antérieure à la date de fin.");
                return;
            }

            Map<String, Object> stats = statistiqueService.genererStatistiques(debut, fin);
            
            double taux = (double) stats.get("tauxOccupationGlobal");
            Label tauxLabel = new Label(String.format("Taux d'occupation global: %.1f%%", taux));
            tauxLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            // Courbe d'évolution
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Nombre de réservations");
            LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("Évolution quotidienne des réservations");
            lineChart.setPrefHeight(300); // Hauteur fixe raisonnable
            lineChart.setAnimated(false);
            lineChart.setMinHeight(250);
            lineChart.setMaxHeight(400);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Réservations");

            List<LocalDate> dates = new ArrayList<>();
            LocalDate current = debut;
            while (!current.isAfter(fin)) {
                dates.add(current);
                current = current.plusDays(1);
            }

            for (LocalDate d : dates) {
                long count = reservationDAO.findByPeriode(d, d).size();
                series.getData().add(new XYChart.Data<>(d.format(DateTimeFormatter.ofPattern("dd/MM")), count));
            }
            lineChart.getData().clear();
            lineChart.getData().add(series);

            // Barres occupation par salle
            CategoryAxis xAxis2 = new CategoryAxis();
            NumberAxis yAxis2 = new NumberAxis();
            yAxis2.setLabel("Nombre de réservations");
            BarChart<String, Number> barChart = new BarChart<>(xAxis2, yAxis2);
            barChart.setTitle("Occupation par salle");
            barChart.setPrefHeight(250);
            barChart.setAnimated(false);
            barChart.setMinHeight(200);
            barChart.setMaxHeight(350);

            XYChart.Series<String, Number> series2 = new XYChart.Series<>();
            series2.setName("Réservations");

            Map<Salle, Long> occParSalle = (Map<Salle, Long>) stats.get("occupationParSalle");
            if (occParSalle.isEmpty()) {
                Label emptyLabel = new Label("Aucune réservation sur cette période.");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                chartsContainer.getChildren().setAll(tauxLabel, emptyLabel);
            } else {
                occParSalle.forEach((salle, count) -> {
                    series2.getData().add(new XYChart.Data<>(salle.getNumero(), count));
                });
                barChart.getData().clear();
                barChart.getData().add(series2);

                // Camembert par type
                PieChart pieChart = new PieChart();
                pieChart.setTitle("Répartition par type");
                pieChart.setPrefHeight(200);
                Map<Reservation.TypeReservation, Long> parType = (Map<Reservation.TypeReservation, Long>) stats.get("parType");
                pieChart.getData().clear();
                parType.forEach((type, count) -> {
                    pieChart.getData().add(new PieChart.Data(type.toString() + " (" + count + ")", count));
                });

                chartsContainer.getChildren().setAll(
                    tauxLabel,
                    new Label("Évolution quotidienne :"),
                    lineChart,
                    new Label("Occupation par salle :"),
                    barChart,
                    new Label("Répartition par type :"),
                    pieChart
                );
            }
        };

        rafraichirBtn.setOnAction(e -> updateCharts.run());
        debutPicker.setOnAction(e -> updateCharts.run());
        finPicker.setOnAction(e -> updateCharts.run());

        updateCharts.run();

        TabPane reportTabs = new TabPane();
        Tab occupationTab = new Tab("Taux d'occupation");
        occupationTab.setContent(scrollPane);  // IMPORTANT : mettre le ScrollPane, pas le VBox directement
        
        // ... reste des onglets Export inchangé ...
        Tab exportTab = new Tab("Export");
        VBox exportBox = new VBox(10);
        exportBox.setPadding(new Insets(10));
        
        Button exportExcelBtn = createStyledButton("Exporter en Excel", "📊", "#27ae60");
        exportExcelBtn.setPrefWidth(200);
        Button exportPDFBtn = createStyledButton("Exporter en PDF", "📄", "#e74c3c");
        exportPDFBtn.setPrefWidth(200);
        Button exportCSVBtn = createStyledButton("Exporter en CSV", "📃", "#3498db");
        exportCSVBtn.setPrefWidth(200);
        
        exportExcelBtn.setOnAction(e -> {
            List<Reservation> reservations = reservationDAO.findByPeriode(debutPicker.getValue(), finPicker.getValue());
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport Excel");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
            File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
            if (file != null) {
                showAlert(Alert.AlertType.INFORMATION, "Info", "Export Excel à implémenter avec Apache POI");
            }
        });
        
        exportPDFBtn.setOnAction(e -> {
            List<Reservation> reservations = reservationDAO.findByPeriode(debutPicker.getValue(), finPicker.getValue());
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
            if (file != null) {
                PdfUtils.exporterReservationsVersPDF(reservations, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Export PDF terminé !");
            }
        });
        
        exportCSVBtn.setOnAction(e -> {
            List<Reservation> reservations = reservationDAO.findByPeriode(debutPicker.getValue(), finPicker.getValue());
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
            File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
            if (file != null) {
                ExportUtils.exporterReservationsVersCSV(reservations, file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Export CSV terminé !");
            }
        });
        
        exportBox.getChildren().addAll(exportExcelBtn, exportPDFBtn, exportCSVBtn);
        exportTab.setContent(exportBox);
        
        reportTabs.getTabs().addAll(occupationTab, exportTab);

        view.getChildren().addAll(title, periodeBox, reportTabs);
        return view;
    }
       // ==================== MÉTHODES UTILITAIRES ====================

    private void setContent(Parent content) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
        
        FadeTransition ft = new FadeTransition(Duration.millis(500), content);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private Button createStyledButton(String text, String icon, String color) {
        Button btn = new Button(icon + " " + text);
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14px; " +
            "-fx-padding: 8 15; -fx-background-radius: 5;", color));
        btn.setOnMouseEntered(e -> 
            btn.setStyle(String.format("-fx-background-color: derive(%s, -20%%); -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;", color)));
        btn.setOnMouseExited(e -> 
            btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 5;", color)));
        return btn;
    }

    private HBox createStatsCards() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);
        
        long nbSalles = salleDAO.findAll().size();
        long nbCours = coursDAO.findAll().size();
        long nbReservations = reservationDAO.findAll().size();
        long nbUtilisateurs = utilisateurDAO.findAll().size();
        
        box.getChildren().addAll(
            createStatCard("Salles", String.valueOf(nbSalles), "#3498db"),
            createStatCard("Cours", String.valueOf(nbCours), "#e74c3c"),
            createStatCard("Réservations", String.valueOf(nbReservations), "#27ae60"),
            createStatCard("Utilisateurs", String.valueOf(nbUtilisateurs), "#f39c12")
        );
        
        return box;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(150);
        card.getStyleClass().add("stat-card");   // ← AJOUTER CETTE LIGNE
        card.setAlignment(Pos.CENTER);

        card.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);",
            color));
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        
        card.setOnMouseEntered(e -> {
            card.setStyle(String.format(
                "-fx-background-color: derive(%s, -20%%); -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 15, 0, 0, 8);",
                color));
        });
        card.setOnMouseExited(e -> {
            card.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);",
                color));
        });
        
        return card;
    }

    private HBox createCharts() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);
        
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Répartition des salles par type");
        pieChart.setPrefWidth(300);
        pieChart.setPrefHeight(250);
        
        long tdCount = salleDAO.findAll().stream()
            .filter(s -> s.getType() == Salle.TypeSalle.TD).count();
        long tpCount = salleDAO.findAll().stream()
            .filter(s -> s.getType() == Salle.TypeSalle.TP).count();
        long amphiCount = salleDAO.findAll().stream()
            .filter(s -> s.getType() == Salle.TypeSalle.AMPHI).count();
        
        pieChart.getData().addAll(
            new PieChart.Data("TD (" + tdCount + ")", tdCount),
            new PieChart.Data("TP (" + tpCount + ")", tpCount),
            new PieChart.Data("Amphi (" + amphiCount + ")", amphiCount)
        );
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Réservations des 7 derniers jours");
        lineChart.setPrefWidth(400);
        lineChart.setPrefHeight(250);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réservations");
        
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long count = reservationDAO.findByPeriode(date, date).size();
            series.getData().add(new XYChart.Data<>(
                date.format(DateTimeFormatter.ofPattern("dd/MM")), count));
        }
        
        lineChart.getData().add(series);
        
        box.getChildren().addAll(pieChart, lineChart);
        return box;
    }

    private VBox createRecentReservations() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label title = new Label("Dernières réservations");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(150);

        List<Reservation> recent = reservationDAO.findAll().stream()
            .filter(r -> r.getStatut() != Reservation.Statut.ANNULE)
            .sorted((r1, r2) -> r2.getDate().compareTo(r1.getDate()))
            .limit(5)
            .collect(Collectors.toList());

        for (Reservation r : recent) {
            String info = String.format("%s - %s %s: %s-%s",
                r.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                r.getCours() != null ? r.getCours().getNom() : "Réunion",
                r.getSalle().getNumero(),
                r.getHeureDebut().format(DateTimeFormatter.ofPattern("HH:mm")),
                r.getHeureFin().format(DateTimeFormatter.ofPattern("HH:mm")));
            listView.getItems().add(info);
        }

        box.getChildren().addAll(title, listView);
        return box;
    }

    // ==================== DIALOGUES CRUD ====================

    private void showAddBatimentDialog(ObservableList<Batiment> data) {
        // ... (inchangé)
    }

    private void showEditBatimentDialog1(Batiment batiment, ObservableList<Batiment> data) {
        // ...
    }

    private void showDeleteBatimentDialog1(Batiment batiment, ObservableList<Batiment> data) {
        // ...
    }

    private void showAddSalleDialog1(ObservableList<Salle> data) {
        // ...
    }

    private void showEditSalleDialog1(Salle salle, ObservableList<Salle> data) {
        // ...
    }

    private void showDeleteSalleDialog1(Salle salle, ObservableList<Salle> data) {
        // ...
    }

    private void showAddEquipementDialog1(ObservableList<Equipement> data) {
        // ...
    }

    private void showEditEquipementDialog1(Equipement equipement, ObservableList<Equipement> data) {
        // ...
    }

    private void showDeleteEquipementDialog1(Equipement equipement, ObservableList<Equipement> data) {
        // ...
    }

    private void showAddCoursDialog1(ObservableList<Cours> data) {
        // ...
    }

    private void showEditCoursDialog1(Cours cours, ObservableList<Cours> data) {
        // ...
    }

    private void showDeleteCoursDialog1(Cours cours, ObservableList<Cours> data) {
        // ...
    }

    // ==================== RÉSERVATIONS ====================

    private void showAddReservationDialog(Salle salle, LocalDate date) {
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une réservation");
        dialog.setHeaderText("Nouvelle réservation");

        ButtonType saveButtonType = new ButtonType("Réserver", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Cours> coursCombo = new ComboBox<>();
        coursCombo.getItems().setAll(coursDAO.findAll());
        coursCombo.setPromptText("Sélectionner un cours");
        
        ComboBox<Salle> salleCombo = new ComboBox<>();
        salleCombo.getItems().setAll(salleDAO.findAll());
        salleCombo.setPromptText("Sélectionner une salle");
        if (salle != null) salleCombo.setValue(salle);
        
        DatePicker datePicker = new DatePicker(date != null ? date : LocalDate.now());
        
        ComboBox<String> heureDebutCombo = new ComboBox<>();
        heureDebutCombo.getItems().addAll("08:00", "09:00", "10:00", "11:00", "12:00", 
                                         "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00");
        heureDebutCombo.setValue("08:00");
        
        ComboBox<String> heureFinCombo = new ComboBox<>();
        heureFinCombo.getItems().addAll("09:00", "10:00", "11:00", "12:00", "13:00", 
                                       "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00");
        heureFinCombo.setValue("10:00");
        
        ComboBox<Reservation.TypeReservation> typeCombo = new ComboBox<>();
        typeCombo.getItems().setAll(Reservation.TypeReservation.values());
        typeCombo.setValue(Reservation.TypeReservation.COURS);

        grid.add(new Label("Cours:"), 0, 0);
        grid.add(coursCombo, 1, 0);
        grid.add(new Label("Salle:"), 0, 1);
        grid.add(salleCombo, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Heure début:"), 0, 3);
        grid.add(heureDebutCombo, 1, 3);
        grid.add(new Label("Heure fin:"), 0, 4);
        grid.add(heureFinCombo, 1, 4);
        grid.add(new Label("Type:"), 0, 5);
        grid.add(typeCombo, 1, 5);

        Label conflitLabel = new Label();
        conflitLabel.setWrapText(true);
        conflitLabel.setMaxWidth(300);
        conflitLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        
        Runnable verifierConflits = () -> {
            try {
                if (salleCombo.getValue() != null && datePicker.getValue() != null &&
                    heureDebutCombo.getValue() != null && !heureDebutCombo.getValue().isEmpty() &&
                    heureFinCombo.getValue() != null && !heureFinCombo.getValue().isEmpty()) {
                    
                    Reservation temp = new Reservation();
                    temp.setSalle(salleCombo.getValue());
                    temp.setDate(datePicker.getValue());
                    temp.setHeureDebut(LocalTime.parse(heureDebutCombo.getValue()));
                    temp.setHeureFin(LocalTime.parse(heureFinCombo.getValue()));
                    if (coursCombo.getValue() != null) {
                        temp.setCours(coursCombo.getValue());
                    }
                    temp.setType(typeCombo.getValue());
                    
                    List<String> conflits = conflitDetector.verifierReservation(temp);
                    if (!conflits.isEmpty()) {
                        conflitLabel.setText("⚠️ " + String.join("\n", conflits));
                        conflitLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        conflitLabel.setText("✓ Créneau disponible");
                        conflitLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }
                }
            } catch (Exception e) {
                conflitLabel.setText("Erreur de vérification");
            }
        };
        
        salleCombo.setOnAction(e -> verifierConflits.run());
        datePicker.setOnAction(e -> verifierConflits.run());
        heureDebutCombo.setOnAction(e -> verifierConflits.run());
        heureFinCombo.setOnAction(e -> verifierConflits.run());

        verifierConflits.run();

        grid.add(conflitLabel, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Reservation r = new Reservation();
                    r.setCours(coursCombo.getValue());
                    r.setSalle(salleCombo.getValue());
                    r.setDate(datePicker.getValue());
                    r.setHeureDebut(LocalTime.parse(heureDebutCombo.getValue()));
                    r.setHeureFin(LocalTime.parse(heureFinCombo.getValue()));
                    r.setType(typeCombo.getValue());
                    r.setStatut(Reservation.Statut.PLANIFIE);
                    return r;
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs correctement");
                    return null;
                }
            }
            return null;
        });

        Optional<Reservation> result = dialog.showAndWait();
        result.ifPresent(reservation -> {
            List<String> conflits = conflitDetector.verifierReservation(reservation);
            if (!conflits.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Conflits détectés", 
                    "Impossible de réserver:\n" + String.join("\n", conflits));
            } else {
                if (reservationDAO.create(reservation)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation effectuée !");
                    notificationService.notifierNouvelleReservation(reservation);
                    refreshEmploiDuTemps();
                }
            }
        });
    }

    private void showEditReservationDialog(Reservation reservation) {
        String ancienneSalle = reservation.getSalle() != null ? reservation.getSalle().toString() : "";
        String ancienneDate = reservation.getDate() != null ? reservation.getDate().toString() : "";
        String ancienHeureDebut = reservation.getHeureDebut() != null ? reservation.getHeureDebut().toString() : "";

        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Modifier une réservation");
        dialog.setHeaderText("Modification de la réservation");

        ButtonType saveButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Cours> coursCombo = new ComboBox<>();
        coursCombo.getItems().setAll(coursDAO.findAll());
        coursCombo.setValue(reservation.getCours());
        
        ComboBox<Salle> salleCombo = new ComboBox<>();
        salleCombo.getItems().setAll(salleDAO.findAll());
        salleCombo.setValue(reservation.getSalle());
        
        DatePicker datePicker = new DatePicker(reservation.getDate());
        
        ComboBox<String> heureDebutCombo = new ComboBox<>();
        heureDebutCombo.getItems().addAll("08:00", "09:00", "10:00", "11:00", "12:00", 
                                         "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00");
        heureDebutCombo.setValue(reservation.getHeureDebut().toString());
        
        ComboBox<String> heureFinCombo = new ComboBox<>();
        heureFinCombo.getItems().addAll("09:00", "10:00", "11:00", "12:00", "13:00", 
                                       "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00");
        heureFinCombo.setValue(reservation.getHeureFin().toString());
        
        ComboBox<Reservation.TypeReservation> typeCombo = new ComboBox<>();
        typeCombo.getItems().setAll(Reservation.TypeReservation.values());
        typeCombo.setValue(reservation.getType());

        grid.add(new Label("Cours:"), 0, 0);
        grid.add(coursCombo, 1, 0);
        grid.add(new Label("Salle:"), 0, 1);
        grid.add(salleCombo, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Heure début:"), 0, 3);
        grid.add(heureDebutCombo, 1, 3);
        grid.add(new Label("Heure fin:"), 0, 4);
        grid.add(heureFinCombo, 1, 4);
        grid.add(new Label("Type:"), 0, 5);
        grid.add(typeCombo, 1, 5);

        Label conflitLabel = new Label();
        conflitLabel.setWrapText(true);
        conflitLabel.setMaxWidth(300);
        conflitLabel.setStyle("-fx-text-fill: green;");
        
        Runnable verifierConflits = () -> {
            try {
                if (salleCombo.getValue() != null && datePicker.getValue() != null &&
                    heureDebutCombo.getValue() != null && !heureDebutCombo.getValue().isEmpty() &&
                    heureFinCombo.getValue() != null && !heureFinCombo.getValue().isEmpty()) {
                    
                    Reservation temp = new Reservation();
                    temp.setSalle(salleCombo.getValue());
                    temp.setDate(datePicker.getValue());
                    temp.setHeureDebut(LocalTime.parse(heureDebutCombo.getValue()));
                    temp.setHeureFin(LocalTime.parse(heureFinCombo.getValue()));
                    if (coursCombo.getValue() != null) temp.setCours(coursCombo.getValue());
                    temp.setType(typeCombo.getValue());
                    
                    List<String> conflits = conflitDetector.verifierReservation(temp);
                    // Ignorer le conflit avec la réservation elle-même
                    conflits.removeIf(c -> c.contains("La salle est déjà occupée") && 
                                           temp.getSalle().equals(reservation.getSalle()) &&
                                           temp.getDate().equals(reservation.getDate()) &&
                                           temp.getHeureDebut().equals(reservation.getHeureDebut()) &&
                                           temp.getHeureFin().equals(reservation.getHeureFin()));
                    
                    if (!conflits.isEmpty()) {
                        conflitLabel.setText("⚠️ " + String.join("\n", conflits));
                        conflitLabel.setStyle("-fx-text-fill: red;");
                    } else {
                        conflitLabel.setText("✓ Créneau disponible");
                        conflitLabel.setStyle("-fx-text-fill: green;");
                    }
                }
            } catch (Exception e) {
                conflitLabel.setText("Erreur de vérification");
            }
        };
        
        salleCombo.setOnAction(e -> verifierConflits.run());
        datePicker.setOnAction(e -> verifierConflits.run());
        heureDebutCombo.setOnAction(e -> verifierConflits.run());
        heureFinCombo.setOnAction(e -> verifierConflits.run());

        verifierConflits.run();

        grid.add(conflitLabel, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    reservation.setCours(coursCombo.getValue());
                    reservation.setSalle(salleCombo.getValue());
                    reservation.setDate(datePicker.getValue());
                    reservation.setHeureDebut(LocalTime.parse(heureDebutCombo.getValue()));
                    reservation.setHeureFin(LocalTime.parse(heureFinCombo.getValue()));
                    reservation.setType(typeCombo.getValue());
                    return reservation;
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs correctement");
                    return null;
                }
            }
            return null;
        });

        Optional<Reservation> result = dialog.showAndWait();
        result.ifPresent(updatedReservation -> {
            List<String> conflits = conflitDetector.verifierReservation(updatedReservation);
            conflits.removeIf(c -> c.contains("La salle est déjà occupée") && 
                                   updatedReservation.getSalle().equals(reservation.getSalle()) &&
                                   updatedReservation.getDate().equals(reservation.getDate()) &&
                                   updatedReservation.getHeureDebut().equals(reservation.getHeureDebut()) &&
                                   updatedReservation.getHeureFin().equals(reservation.getHeureFin()));
            
            if (!conflits.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Conflits détectés", 
                    "Impossible de modifier:\n" + String.join("\n", conflits));
            } else {
                if (reservationDAO.update(updatedReservation)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation modifiée !");
                    notificationService.notifierModificationReservation(updatedReservation, 
                        ancienneSalle, ancienneDate, ancienHeureDebut);
                    refreshEmploiDuTemps();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier la réservation");
                }
            }
        });
    }

    private void showReservationOptionsDialog(Reservation reservation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Options de réservation");
        alert.setHeaderText("Que souhaitez-vous faire ?");
        alert.setContentText("Réservation: " + (reservation.getCours() != null ? reservation.getCours().getNom() : "N/A") + 
                             " le " + reservation.getDate() + " de " + reservation.getHeureDebut() + " à " + reservation.getHeureFin());

        ButtonType modifierBtn = new ButtonType("Modifier");
        ButtonType annulerBtn = new ButtonType("Annuler");
        ButtonType fermerBtn = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(modifierBtn, annulerBtn, fermerBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == modifierBtn) {
                showEditReservationDialog(reservation);
            } else if (result.get() == annulerBtn) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmation");
                confirm.setHeaderText(null);
                confirm.setContentText("Voulez-vous vraiment annuler cette réservation ?");
                Optional<ButtonType> response = confirm.showAndWait();
                if (response.isPresent() && response.get() == ButtonType.OK) {
                    reservation.setStatut(Reservation.Statut.ANNULE);
                    if (reservationDAO.update(reservation)) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation annulée !");
                        notificationService.notifierAnnulationReservation(reservation);
                        refreshEmploiDuTemps();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'annuler la réservation");
                    }
                }
            }
        }
    }

    private void refreshEmploiDuTemps() {
        if (contentArea.getChildren().size() > 0) {
            Parent current = (Parent) contentArea.getChildren().get(0);
            if (current instanceof VBox) {
                // On suppose que c'est la vue emploi du temps si elle a un titre "Emploi du Temps"
                // Pour simplifier, on recharge la vue
                showEmploiDuTemps();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}
