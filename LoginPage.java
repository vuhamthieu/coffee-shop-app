import javafx.application.Application;
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

public class LoginPage extends Application {
    
    // Màu sắc theo palette của bạn
    private static final String PRIMARY_COLOR = "#6B4C3B";    // Nâu cà phê
    private static final String SECONDARY_COLOR = "#F5F0E1"; // Kem nhạt
    private static final String ACCENT_COLOR = "#F2C57C";    // Vàng nhạt
    private static final String TEXT_COLOR = "#3E2D21";      // Nâu đậm
    
    private Stage primaryStage;
    private TextField usernameField;
    private PasswordField passwordField;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";");
        
        // Tạo các thành phần
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
        
        // Logo Coffee Shop đơn giản
        VBox logoBox = new VBox(8);
        logoBox.setAlignment(Pos.CENTER);
        
        // Logo icon lớn
        Text coffeeIcon = new Text("☕");
        coffeeIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        coffeeIcon.setFill(Color.web(ACCENT_COLOR));
        
        // Tên tiệm cà phê
        Text shopName = new Text("COFFEE BEANS");
        shopName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        shopName.setFill(Color.WHITE);
        
        // Separator nhỏ
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
        
        // Card form
        VBox card = new VBox(25);
        card.setPadding(new Insets(40, 35, 40, 35));
        card.setStyle("-fx-background-color: white;" +
                     "-fx-background-radius: 15;" +
                     "-fx-border-color: " + PRIMARY_COLOR + ";" +
                     "-fx-border-width: 2;" +
                     "-fx-border-radius: 15;" +
                     "-fx-effect: dropshadow(gaussian, rgba(107,76,59,0.15), 20, 0, 0, 8);");
        
        // Tiêu đề form với icon
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);
        
        Text userIcon = new Text("👤");
        userIcon.setFont(Font.font(20));
        
        Text formTitle = new Text("ĐĂNG NHẬP HỆ THỐNG");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        formTitle.setFill(Color.web(TEXT_COLOR));
        
        titleBox.getChildren().addAll(userIcon, formTitle);
        
        // Tên đăng nhập
        VBox usernameBox = new VBox(8);
        Label usernameLabel = new Label("Tên đăng nhập:");
        usernameLabel.setStyle("-fx-text-fill: " + TEXT_COLOR + "; -fx-font-weight: bold;");
        
        usernameField = new TextField();
        usernameField.setPrefHeight(42);
        usernameField.setStyle("-fx-background-radius: 8;" +
                              "-fx-border-color: #D4C9B8;" +
                              "-fx-border-radius: 8;" +
                              "-fx-border-width: 1.5;" +
                              "-fx-padding: 0 15 0 15;" +
                              "-fx-font-size: 14;");
        usernameField.setPromptText("admin / staff");
        
        usernameBox.getChildren().addAll(usernameLabel, usernameField);
        
        // Mật khẩu
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Mật khẩu:");
        passwordLabel.setStyle("-fx-text-fill: " + TEXT_COLOR + "; -fx-font-weight: bold;");
        
        passwordField = new PasswordField();
        passwordField.setPrefHeight(42);
        passwordField.setStyle("-fx-background-radius: 8;" +
                              "-fx-border-color: #D4C9B8;" +
                              "-fx-border-radius: 8;" +
                              "-fx-border-width: 1.5;" +
                              "-fx-padding: 0 15 0 15;" +
                              "-fx-font-size: 14;");
        passwordField.setPromptText("password123");
        
        passwordBox.getChildren().addAll(passwordLabel, passwordField);
        
        // Nút đăng nhập
        Button loginButton = new Button("ĐĂNG NHẬP");
        loginButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 16;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 12 0 12 0;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(107,76,59,0.2), 8, 0, 0, 2);");
        loginButton.setPrefWidth(250);
        loginButton.setPrefHeight(45);
        
        // Xử lý đăng nhập - CHUYỂN SANG App.java
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            if (authenticate(username, password)) {
                // Đăng nhập thành công - chuyển sang App.java
                openAppPage(username);
            } else {
                showAlert("Lỗi đăng nhập", "Tên đăng nhập hoặc mật khẩu không đúng!");
            }
        });
        
        // Enter key để đăng nhập
        passwordField.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            if (authenticate(username, password)) {
                openAppPage(username);
            } else {
                showAlert("Lỗi đăng nhập", "Tên đăng nhập hoặc mật khẩu không đúng!");
            }
        });
        
        // Hover effect
        loginButton.setOnMouseEntered(e -> {
            loginButton.setStyle("-fx-background-color: #5A4030;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 16;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 12 0 12 0;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(90,64,48,0.3), 10, 0, 0, 3);");
        });
        
        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 16;" +
                                "-fx-background-radius: 8;" +
                                "-fx-padding: 12 0 12 0;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(107,76,59,0.2), 8, 0, 0, 2);");
        });
        
        HBox loginBtnBox = new HBox();
        loginBtnBox.setAlignment(Pos.CENTER);
        loginBtnBox.getChildren().add(loginButton);

        card.getChildren().addAll(titleBox, usernameBox, passwordBox, loginBtnBox);
        form.getChildren().add(card);
        
        return form;
    }
    
    private void openAppPage(String username) {
        try {
            // Đóng LoginPage
            primaryStage.close();
            
            // Tạo Stage mới cho App
            Stage appStage = new Stage();
            
            // Tạo instance của App và chạy
            App app = new App();
            app.start(appStage);
            
            // Có thể set title cho App
            appStage.setTitle("Coffee Beans Cafe - Dashboard (" + username + ")");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở trang chính: " + e.getMessage());
            
            // Nếu lỗi, mở lại LoginPage
            primaryStage.show();
        }
    }
    
    private VBox createFooter() {
        VBox footer = new VBox(20);
        footer.setPadding(new Insets(30, 40, 40, 40));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: white;" +
                       "-fx-border-color: #E8DFD0;" +
                       "-fx-border-width: 1 0 0 0;");
        
        // Separator decorative
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
        
        // (Check In/Out button removed)
        
        // Footer note đơn giản
        Label footerNote = new Label("Coffee Beans Cafe");
        footerNote.setFont(Font.font("Segoe UI", 12));
        footerNote.setTextFill(Color.web("#8C7C6C"));
        
        // CHỈ CÒN separator và footer note
        footer.getChildren().addAll(separatorBox, footerNote);
        return footer;
    }
    
    private boolean authenticate(String username, String password) {
        // Demo authentication
        String adminUser = "admin";
        String adminPass = "admin123";
        
        String staffUser = "staff";
        String staffPass = "staff123";
        
        return (username.equals(adminUser) && password.equals(adminPass)) ||
               (username.equals(staffUser) && password.equals(staffPass));
    }
    
    private void runPythonScript() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "recognize_and_log.py");
            pb.redirectErrorStream(true);
            
            new Thread(() -> {
                try {
                    Process process = pb.start();
                    
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                    );
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[AI System]: " + line);
                    }
                    
                    int exitCode = process.waitFor();
                    System.out.println("Python script exited with code: " + exitCode);
                    
                    // Hiển thị thông báo khi hoàn thành
                    javafx.application.Platform.runLater(() -> {
                        if (exitCode == 0) {
                            showAlert("Hoàn thành", "Quá trình điểm danh đã hoàn tất!");
                        } else {
                            showAlert("Thông báo", "Quá trình điểm danh đã kết thúc.");
                        }
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> 
                        showAlert("Lỗi", "Không thể chạy script: " + e.getMessage())
                    );
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
        dialogPane.setStyle("-fx-background-color: white;" +
                           "-fx-font-family: 'Segoe UI';" +
                           "-fx-border-color: " + ACCENT_COLOR + ";" +
                           "-fx-border-width: 2;" +
                           "-fx-border-radius: 5;");
        
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}