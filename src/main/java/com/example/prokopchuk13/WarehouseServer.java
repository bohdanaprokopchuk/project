package com.example.prokopchuk13;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Properties;

public class WarehouseServer {
    private static final int PORT = 1234;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/warehouse";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                while (true) {
                    String action = (String) ois.readObject();
                    switch (action) {
                        case "ADD_PRODUCT":
                            handleAddProduct(connection);
                            break;
                        case "EDIT_PRODUCT":
                            handleEditProduct(connection);
                            break;
                        case "DELETE_PRODUCT":
                            handleDeleteProduct(connection);
                            break;
                        case "SEARCH_PRODUCT":
                            handleSearchProduct(connection);
                            break;
                        case "ADD_PRODUCT_GROUP":
                            handleAddProductGroup(connection);
                            break;
                        case "EDIT_PRODUCT_GROUP":
                            handleEditProductGroup(connection);
                            break;
                        case "DELETE_PRODUCT_GROUP":
                            handleDeleteProductGroup(connection);
                            break;
                        case "SEARCH_PRODUCT_GROUP":
                            handleSearchProductGroup(connection);
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            } finally {
                closeResources();
            }
        }

        private void handleAddProduct(Connection connection) {
            try {
                Product product = (Product) ois.readObject();
                String query = "INSERT INTO products (name, description, manufacturer, quantity, price, group_id) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, product.getName());
                    pstmt.setString(2, product.getDescription());
                    pstmt.setString(3, product.getManufacturer());
                    pstmt.setInt(4, product.getQuantity());
                    pstmt.setDouble(5, product.getPrice());
                    pstmt.setInt(6, product.getGroupId());
                    pstmt.executeUpdate();
                    sendResponse("Product added successfully");
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                handleError(e, "Failed to add product");
            }
        }

        private void handleEditProduct(Connection connection) {
            try {
                Product product = (Product) ois.readObject();
                String query = "UPDATE products SET description = ?, manufacturer = ?, quantity = ?, price = ?, group_id = ? WHERE name = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, product.getDescription());
                    pstmt.setString(2, product.getManufacturer());
                    pstmt.setInt(3, product.getQuantity());
                    pstmt.setDouble(4, product.getPrice());
                    pstmt.setInt(5, product.getGroupId());
                    pstmt.setString(6, product.getName());
                    pstmt.executeUpdate();
                    sendResponse("Product updated successfully");
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                handleError(e, "Failed to update product");
            }
        }

        private void handleDeleteProduct(Connection connection) {
            try {
                String name = (String) ois.readObject();
                String query = "DELETE FROM products WHERE name = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, name);
                    pstmt.executeUpdate();
                    sendResponse("Product deleted successfully");
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                handleError(e, "Failed to delete product");
            }
        }

        private void handleSearchProduct(Connection connection) {
            try {
                String name = (String) ois.readObject();
                String query = "SELECT * FROM products WHERE name = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, name);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            Product product = new Product(
                                    rs.getString("name"),
                                    rs.getString("description"),
                                    rs.getString("manufacturer"),
                                    rs.getInt("quantity"),
                                    rs.getDouble("price"),
                                    rs.getInt("group_id")
                            );
                            oos.writeObject("Product found: " + product);
                        } else {
                            sendResponse("Product not found");
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                handleError(e, "Failed to search product");
            }
        }

        private void handleAddProductGroup(Connection connection) {
            try {
                ProductGroup productGroup = (ProductGroup) ois.readObject();
                String query = "INSERT INTO product_groups (name, description) VALUES (?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, productGroup.getName());
                    pstmt.setString(2, productGroup.getDescription());
                    pstmt.executeUpdate();
                    sendResponse("Product group added successfully");
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                handleError(e, "Failed to add product group");
            }
        }

        private void handleEditProductGroup(Connection connection) {
            try {
                ProductGroup productGroup = (ProductGroup) ois.readObject();
                String query = "UPDATE product_groups SET description = ? WHERE name = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, productGroup.getDescription());
                    pstmt.setString(2, productGroup.getName());
                    pstmt.executeUpdate();
                    sendResponse("Product group updated successfully");
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                handleError(e, "Failed to update product group");
            }
        }

        private void handleDeleteProductGroup(Connection connection) {
            try {
                String name = (String) ois.readObject();
                String query = "DELETE FROM product_groups WHERE name = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, name);
                    pstmt.executeUpdate();
                    sendResponse("Product group deleted successfully");
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                handleError(e, "Failed to delete product group");
            }
        }

        private void handleSearchProductGroup(Connection connection) {
            try {
                String name = (String) ois.readObject();
                String query = "SELECT * FROM product_groups WHERE name = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, name);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            ProductGroup productGroup = new ProductGroup(
                                    rs.getString("name"),
                                    rs.getString("description")
                            );
                            oos.writeObject("Product group found: " + productGroup);
                        } else {
                            sendResponse("Product group not found");
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                handleError(e, "Failed to search product group");
            }
        }

        private void sendResponse(String message) {
            try {
                oos.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleError(Exception e, String errorMessage) {
            e.printStackTrace();
            sendResponse(errorMessage);
        }

        private void closeResources() {
            try {
                if (ois != null) ois.close();
                if (oos != null) oos.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
