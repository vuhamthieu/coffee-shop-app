import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FaceApp extends Application {

    @Override
    public void start(Stage stage) {
        Label status = new Label("Nhấn nút để lấy ảnh khuôn mặt");

        Button btn = new Button("Lấy ảnh FaceID");
        btn.setOnAction(e -> {
            status.setText("📸 Đang mở camera...");
            btn.setDisable(true);

            new Thread(() -> {
                String result = runPythonCollect("EMP001");

                Platform.runLater(() -> {
                    status.setText(result);
                    btn.setDisable(false);
                });
            }).start();
        });

        VBox root = new VBox(15, status, btn);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 400, 200);
        stage.setTitle("FaceID - JavaFX");
        stage.setScene(scene);
        stage.show();
    }
 private void logError(String message) {
        try (FileWriter fw = new FileWriter("error_log.txt", true)) { // true = append
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            fw.write("[" + timestamp + "] " + message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Chạy file Python collect_faces.py
     */
    private String runPythonCollect(String employeeName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    "capture_faces.py",   // đường dẫn file py
                    employeeName                 // argv[1]
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return " Lấy ảnh thành công\n" + output.toString();
            } else {
                return " Lấy ảnh thất bại\n" + output.toString();
            }

        } catch (Exception e) {
            return " Lỗi chạy Python: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
