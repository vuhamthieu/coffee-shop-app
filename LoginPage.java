import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginPage extends Application {

    // API URL
    private static final String LOGIN_URL = "http://localhost/coffee-shop-app/backend/api/employee/login.php";

    // Màu sắc
    private static final String PRIMARY_COLOR = "#6B4C3B";
    private static final String SECONDARY_COLOR = "#F5F0E1";
    private static final String ACCENT_COLOR = "#F2C57C";
    private static final String TEXT_COLOR = "#3E2D21";

    private Stage primaryStage;
    private TextField usernameField;
    private PasswordField passwordField;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";");

        VBox header = createHeader();
        VBox loginForm = createLoginForm();
        VBox footer = createFooter();

        root.setTop(header);
        root.setCenter(loginForm);
        root.setBottom(footer);

        Scene scene = new Scene(root, 900, 650);
        primaryStage.setTitle("Coffee Shop - Employee Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(30, 25, 30, 25));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";");

        VBox logoBox = new VBox(8);
        logoBox.setAlignment(Pos.CENTER);

        Text coffeeIcon = new Text("☕");
        coffeeIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        coffeeIcon.setFill(Color.web(ACCENT_COLOR));

        Text shopName = new Text("COFFEE BEANS");
        shopName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        shopName.setFill(Color.WHITE);

        Rectangle separator = new Rectangle(100, 3);
        separator.setFill(Color.web(ACCENT_COLOR));
        separator.setArcWidth(10);
        separator.setArcHeight(10);

        logoBox.getChildren().addAll(coffeeIcon, shopName, separator);
        header.getChildren().addAll(logoBox);
        return header;
    }

    private VBox createLoginForm() {
        VBox form = new VBox(30);
        form.setPadding(new Insets(40));
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(400);

        VBox card = new VBox(25);
        card.setPadding(new Insets(40, 35, 40, 35));
        card.setStyle("-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-border-color: " + PRIMARY_COLOR + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(107,76,59,0.15), 20, 0, 0, 8);");

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);

        Text userIcon = new Text("👤");
        userIcon.setFont(Font.font(20));

        Text formTitle = new Text("ĐĂNG NHẬP HỆ THỐNG");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        formTitle.setFill(Color.web(TEXT_COLOR));

        titleBox.getChildren().addAll(userIcon, formTitle);

        // Username
        VBox usernameBox = new VBox(8);
        Label usernameLabel = new Label("Tên đăng nhập:");
        usernameLabel.setStyle("-fx-text-fill: " + TEXT_COLOR + "; -fx-font-weight: bold;");

        usernameField = new TextField();
        usernameField.setPrefHeight(42);
        usernameField.setStyle(
                "-fx-background-radius: 8; -fx-border-color: #D4C9B8; -fx-border-radius: 8; -fx-padding: 0 15 0 15; -fx-font-size: 14;");
        usernameField.setPromptText("Nhập username");
        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Password
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Mật khẩu:");
        passwordLabel.setStyle("-fx-text-fill: " + TEXT_COLOR + "; -fx-font-weight: bold;");

        passwordField = new PasswordField();
        passwordField.setPrefHeight(42);
        passwordField.setStyle(
                "-fx-background-radius: 8; -fx-border-color: #D4C9B8; -fx-border-radius: 8; -fx-padding: 0 15 0 15; -fx-font-size: 14;");
        passwordField.setPromptText("Nhập password");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Login Button
        Button loginButton = new Button("ĐĂNG NHẬP");
        loginButton.setStyle("-fx-background-color: " + PRIMARY_COLOR
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8; -fx-padding: 12 0 12 0; -fx-cursor: hand;");
        loginButton.setPrefWidth(250);
        loginButton.setPrefHeight(45);

        // Sự kiện đăng nhập
        loginButton.setOnAction(e -> performLogin());
        passwordField.setOnAction(e -> performLogin());

        // Hover effects
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(
                "-fx-background-color: #5A4030; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8; -fx-padding: 12 0 12 0;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: " + PRIMARY_COLOR
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8; -fx-padding: 12 0 12 0;"));

        HBox loginBtnBox = new HBox();
        loginBtnBox.setAlignment(Pos.CENTER);
        loginBtnBox.getChildren().add(loginButton);

        card.getChildren().addAll(titleBox, usernameBox, passwordBox, loginBtnBox);
        form.getChildren().add(card);

        return form;
    }

    // --- LOGIC ĐĂNG NHẬP GỌI API ---
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Tạo JSON body
        String jsonBody = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

        HttpRequest request = HttpRequest.newBuilder(URI.create(LOGIN_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                System.out.println("Login Response: " + body);

                if (response.statusCode() == 200 && body.contains("\"success\":true")) {
                    // Parse role từ JSON response đơn giản
                    String roleName = extractJsonValue(body, "role_name");

                    Platform.runLater(() -> handleLoginSuccess(roleName, username));
                } else {
                    // Lấy thông báo lỗi từ server nếu có
                    String message = extractJsonValue(body, "message");
                    Platform.runLater(() -> showAlert("Đăng nhập thất bại",
                            message.isEmpty() ? "Sai thông tin đăng nhập" : message));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showAlert("Lỗi kết nối", "Không thể kết nối đến server: " + ex.getMessage()));
            }
        }).start();
    }

    private void handleLoginSuccess(String role, String username) {
        try {
            primaryStage.close();
            Stage appStage = new Stage();

            // Phân quyền: Admin vào Dashboard, Staff/Cashier vào POS (App)
            if ("Admin".equalsIgnoreCase(role)) {
                new AdminDashboard().start(appStage);
                appStage.setTitle("Admin Dashboard - " + username);
            } else {
                new App().start(appStage);
                appStage.setTitle("POS Bán Hàng - " + username);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở ứng dụng: " + e.getMessage());
            primaryStage.show();
        }
    }

    // Helper parse JSON đơn giản (để không cần thư viện ngoài)
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start < 0)
            return "";

        start += pattern.length();
        // Bỏ qua khoảng trắng hoặc dấu nháy
        while (start < json.length()
                && (json.charAt(start) == ' ' || json.charAt(start) == '"' || json.charAt(start) == ':')) {
            start++;
        }

        int end = start;
        while (end < json.length() && json.charAt(end) != '"' && json.charAt(end) != ',' && json.charAt(end) != '}') {
            end++;
        }

        return json.substring(start, end).trim();
    }

    private VBox createFooter() {
        VBox footer = new VBox(20);
        footer.setPadding(new Insets(30, 40, 40, 40));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: white; -fx-border-color: #E8DFD0; -fx-border-width: 1 0 0 0;");

        HBox separatorBox = new HBox(10);
        separatorBox.setAlignment(Pos.CENTER);

        Rectangle leftSep = new Rectangle(40, 2);
        leftSep.setFill(Color.web(PRIMARY_COLOR));
        Rectangle midSep = new Rectangle(8, 8);
        midSep.setArcWidth(4);
        midSep.setArcHeight(4);
        midSep.setFill(Color.web(ACCENT_COLOR));
        Rectangle rightSep = new Rectangle(40, 2);
        rightSep.setFill(Color.web(PRIMARY_COLOR));

        separatorBox.getChildren().addAll(leftSep, midSep, rightSep);

        Button checkButton = new Button("Điểm danh (FaceID)");
        checkButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: " + TEXT_COLOR
                + "; -fx-font-weight: bold; -fx-cursor: hand;");
        checkButton.setOnAction(e -> runPythonScript());

        Label footerNote = new Label("Coffee Beans Cafe System");
        footerNote.setFont(Font.font("Segoe UI", 12));
        footerNote.setTextFill(Color.web("#8C7C6C"));

        footer.getChildren().addAll(separatorBox, checkButton, footerNote);
        return footer;
    }

    private void runPythonScript() {
        // Giữ nguyên logic chạy python của bạn
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "recognize_and_log.py");
            pb.redirectErrorStream(true);

            new Thread(() -> {
                try {
                    Process process = pb.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[AI System]: " + line);
                    }
                    int exitCode = process.waitFor();

                    Platform.runLater(() -> {
                        if (exitCode == 0)
                            showAlert("Hoàn thành", "Điểm danh thành công!");
                        else
                            showAlert("Thông báo", "Kết thúc điểm danh.");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Lỗi", "Lỗi script: " + e.getMessage()));
                }
            }).start();
        } catch (Exception e) {
            showAlert("Lỗi hệ thống", "Không thể khởi chạy: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI'; -fx-border-color: "
                + ACCENT_COLOR + "; -fx-border-width: 2;");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}