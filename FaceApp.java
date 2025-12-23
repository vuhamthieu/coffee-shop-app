import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FaceApp extends Application {

    @Override
    public void start(Stage stage) {
        Label status = new Label("Nhấn nút để nhận diện");

        Button btn = new Button("Nhận diện gương mặt");
        btn.setOnAction(e -> {
            status.setText("Đang nhận diện...");
            String result = callAPI();
            status.setText(result);
        });

        VBox root = new VBox(15, status, btn);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 350, 200);
        stage.setTitle("FaceID - JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    private String callAPI() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI("http://127.0.0.1:8000/recognize"))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            return res.body();

        } catch (Exception ex) {
            return "Lỗi API: " + ex.getMessage();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
