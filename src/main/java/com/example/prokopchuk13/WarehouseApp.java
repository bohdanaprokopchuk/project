package com.example.prokopchuk13;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class WarehouseApp extends Application {
    private static final String HOST = "localhost";
    private static final int PORT = 1234;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;

    @Override
    public void start(Stage primaryStage) {
        if (!setupConnection()) {
            showAlert("Error", "Unable to connect to server. Please try again later.");
            return;
        }

        primaryStage.setTitle("Warehouse Management System");

        TabPane tabPane = new TabPane();

        Tab productTab = new Tab("Products", createProductPane());
        Tab productGroupTab = new Tab("Product Groups", createProductGroupPane());

        tabPane.getTabs().addAll(productTab, productGroupTab);

        Scene scene = new Scene(tabPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean setupConnection() {
        try {
            socket = new Socket(HOST, PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private VBox createProductPane() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label titleLabel = new Label("Manage Products");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField nameField = createTextField(gridPane, "Name:", 0);
        TextField descriptionField = createTextField(gridPane, "Description:", 1);
        TextField manufacturerField = createTextField(gridPane, "Manufacturer:", 2);
        TextField quantityField = createTextField(gridPane, "Quantity:", 3);
        TextField priceField = createTextField(gridPane, "Price:", 4);
        TextField groupIdField = createTextField(gridPane, "Group ID:", 5);

        HBox buttonBox = createButtonBox();
        Button addButton = createButton("Add Product", e -> handleAddProduct(
                nameField.getText(),
                descriptionField.getText(),
                manufacturerField.getText(),
                Integer.parseInt(quantityField.getText()),
                Double.parseDouble(priceField.getText()),
                Integer.parseInt(groupIdField.getText())
        ));
        Button editButton = createButton("Edit Product", e -> handleEditProduct(
                nameField.getText(),
                descriptionField.getText(),
                manufacturerField.getText(),
                Integer.parseInt(quantityField.getText()),
                Double.parseDouble(priceField.getText()),
                Integer.parseInt(groupIdField.getText())
        ));
        Button deleteButton = createButton("Delete Product", e -> handleDeleteProduct(nameField.getText()));
        Button searchButton = createButton("Search Product", e -> handleSearchProduct(nameField.getText()));

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton, searchButton);

        vbox.getChildren().addAll(titleLabel, gridPane, buttonBox);
        return vbox;
    }

    private VBox createProductGroupPane() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label titleLabel = new Label("Manage Product Groups");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        TextField nameField = createTextField(gridPane, "Name:", 0);
        TextField descriptionField = createTextField(gridPane, "Description:", 1);

        HBox buttonBox = createButtonBox();
        Button addButton = createButton("Add Product Group", e -> handleAddProductGroup(nameField.getText(), descriptionField.getText()));
        Button editButton = createButton("Edit Product Group", e -> handleEditProductGroup(nameField.getText(), descriptionField.getText()));
        Button deleteButton = createButton("Delete Product Group", e -> handleDeleteProductGroup(nameField.getText()));
        Button searchButton = createButton("Search Product Group", e -> handleSearchProductGroup(nameField.getText()));

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton, searchButton);

        vbox.getChildren().addAll(titleLabel, gridPane, buttonBox);
        return vbox;
    }

    private TextField createTextField(GridPane gridPane, String labelText, int rowIndex) {
        Label label = new Label(labelText);
        TextField textField = new TextField();
        gridPane.add(label, 0, rowIndex);
        gridPane.add(textField, 1, rowIndex);
        return textField;
    }

    private HBox createButtonBox() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10, 0, 0, 0));
        return hbox;
    }

    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> eventHandler) {
        Button button = new Button(text);
        button.setOnAction(eventHandler);
        return button;
    }

    private void handleAddProduct(String name, String description, String manufacturer, int quantity, double price, int group_id) {
        Product product = new Product(name, description, manufacturer, quantity, price, group_id);
        try {
            sendRequest("ADD_PRODUCT", product);
        } catch (Exception e) {
            reconnectAndRetry(() -> handleAddProduct(name, description, manufacturer, quantity, price, group_id));
        }
    }

    private void handleEditProduct(String name, String description, String manufacturer, int quantity, double price, int groupId) {
        Product product = new Product(name, description, manufacturer, quantity, price, groupId);
        try {
            sendRequest("EDIT_PRODUCT", product);
        } catch (Exception e) {
            reconnectAndRetry(() -> handleEditProduct(name, description, manufacturer, quantity, price, groupId));
        }
    }

    private void handleDeleteProduct(String name) {
        try {
            sendRequest("DELETE_PRODUCT", name);
        } catch (Exception e) {
            reconnectAndRetry(() -> handleDeleteProduct(name));
        }
    }

    private void handleSearchProduct(String name) {
        try {
            sendRequest("SEARCH_PRODUCT", name);
        } catch (Exception e) {
            reconnectAndRetry(() -> handleSearchProduct(name));
        }
    }

    private void handleAddProductGroup(String name, String description) {
        ProductGroup productGroup = new ProductGroup(name, description);
        try {
            sendRequest("ADD_PRODUCT_GROUP", productGroup);
        } catch (Exception e) {
            reconnectAndRetry(() -> handleAddProductGroup(name, description));
        }
    }

    private void handleEditProductGroup(String name, String description) {
        ProductGroup productGroup = new ProductGroup(name, description);
        try {
            sendRequest("EDIT_PRODUCT_GROUP", productGroup);
        } catch (Exception e) {
            reconnectAndRetry(() -> handleEditProductGroup(name, description));
        }
    }

    private void handleDeleteProductGroup(String name) {
        try {
            sendRequest("DELETE_PRODUCT_GROUP", name);
        } catch (Exception e) {
            reconnectAndRetry(() -> handleDeleteProductGroup(name));
        }
    }

    private void handleSearchProductGroup(String name) {
        try {
            sendRequest("SEARCH_PRODUCT_GROUP", name);
        } catch (Exception e) {
            reconnectAndRetry(() -> handleSearchProductGroup(name));
        }
    }

    private void sendRequest(String action, Object data) throws IOException, ClassNotFoundException {
        oos.writeObject(action);
        oos.writeObject(data);
        String response = (String) ois.readObject();
        showAlert("Response", response);
    }

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private int retryCount = 0;

    private void reconnectAndRetry(Runnable action) {
        if (retryCount < MAX_RETRY_ATTEMPTS) {
            retryCount++;
            closeConnection();
            if (setupConnection()) {
                action.run();
            } else {
                showAlert("Error", "Failed to reconnect. Please try again later.");
            }
        } else {
            showAlert("Error", "Failed to reconnect after multiple attempts. Please try again later.");
            retryCount = 0;
        }
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (oos != null) oos.close();
            if (ois != null) ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
