import com.example.prokopchuk13.Product;
import com.example.prokopchuk13.WarehouseServer;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.*;

public class WarehouseServerTest {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/warehouse";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "ol1234567!";
    private static final int PORT = 1234;
    private ExecutorService executor;

    @BeforeEach
    void setup() {
        executor = Executors.newCachedThreadPool();
        startServer();
    }

    @AfterEach
    void tearDown() {
        stopServer();
        executor.shutdown();
    }

    private void startServer() {
        executor.submit(() -> {
            try {
                WarehouseServer.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopServer() {
        try (Socket socket = new Socket("localhost", PORT)) {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testHandleAddProduct() throws IOException, ClassNotFoundException, SQLException {
        Socket socket = new Socket("localhost", PORT);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        oos.writeObject("ADD_PRODUCT");

        Product product = new Product("TestProduct", "TestDescription", "TestManufacturer", 10, 99.99, 1);
        oos.writeObject(product);

        String response = (String) ois.readObject();
        assertEquals("Product added successfully", response);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM products WHERE name = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, "TestProduct");
                assertTrue(pstmt.executeQuery().next());
            }
        }
    }

    @Test
    void testHandleSearchProduct() throws IOException, ClassNotFoundException {
        Socket socket = new Socket("localhost", PORT);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        oos.writeObject("SEARCH_PRODUCT");

        oos.writeObject("TestProduct");

        String response = (String) ois.readObject();
        assertTrue(response.contains("Product found"));

        assertTrue(response.contains("TestProduct"));
        assertTrue(response.contains("TestDescription"));
        assertTrue(response.contains("TestManufacturer"));
    }
}
