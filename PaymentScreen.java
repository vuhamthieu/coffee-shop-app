import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PaymentScreen extends Application {

    public interface PaymentListener {
        void onCompleted(String table, double total, String method, LocalDateTime time);
    }

    private final ObservableList<InvoiceLine> invoiceLines;
    private final String tableLabel;
    private final String note;
    private final PaymentListener listener;
    private final int orderId;
    private Stage window;
    private int currentCouponId = -1;

    private final Map<String, Double> discountCodes = new HashMap<>() {
        {
            put("AURA10", 0.10);
            put("MEMBER15", 0.15);
            put("HAPPY50K", 50000d);
        }
    };

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    private Label subtotalLabel;
    private Label taxLabel;
    private Label serviceLabel;
    private Label discountLabel;
    private Label totalLabel;
    private TextField discountField;
    private ToggleGroup paymentGroup;
    private TextArea invoicePreview;
    private double appliedDiscount = 0;

    // HTTP client cho các call backend liên quan đến thanh toán
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // URL sinh PDF hoá đơn từ backend (sửa lại path cho đúng với file PHP của bạn)
    private static final String INVOICE_PDF_URL = "http://localhost:8080/backend/api/employee/print-invoice.php";
    // URL in tem món (labels) từ backend
    private static final String ITEM_LABELS_PDF_URL = "http://localhost:8080/backend/api/employee/print-item-labels.php";
    // URL apply coupon vào backend
    private static final String APPLY_COUPON_URL = "http://localhost:8080/backend/api/employee/apply-coupon.php";
    // URL validate coupon theo order_total (validate-coupon.php trên backend)
    private static final String VALIDATE_COUPON_URL = "http://localhost:8080/backend/api/employee/check-coupon.php";
    // URL remove coupon from order on backend
    private static final String REMOVE_COUPON_URL = "http://localhost:8080/backend/api/employee/remove-coupon.php";

    public PaymentScreen() {
        this("Bàn demo", FXCollections.observableArrayList(
                new InvoiceLine("Cà phê sữa đá", 2, 39000),
                new InvoiceLine("Cold brew cam sả", 1, 52000),
                new InvoiceLine("Bánh croissant bơ nâu", 1, 42000)),
                "Không có", null, -1);
    }

    public PaymentScreen(String tableLabel, ObservableList<InvoiceLine> invoiceLines, String note,
            PaymentListener listener, int orderId) {
        this.tableLabel = tableLabel;
        this.invoiceLines = invoiceLines == null ? FXCollections.observableArrayList() : invoiceLines;
        this.note = note == null ? "" : note;
        this.listener = listener;
        this.orderId = orderId;
    }

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        Scene scene = buildScene();
        primaryStage.setTitle("Coffee Aura • Thanh toán");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        initAfterScene();
    }

    public void showStandalone() {
        Stage stage = new Stage(StageStyle.DECORATED);
        this.window = stage;
        stage.setTitle("Coffee Aura • Thanh toán");
        stage.setScene(buildScene());
        stage.setMaximized(true);
        stage.show();
        initAfterScene();
    }

    private void initAfterScene() {
        refreshSummary();
        refreshPreview();
    }

    private Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color:#F5F0E1;-fx-font-family:'Segoe UI',sans-serif;");

        VBox header = buildHeader();
        BorderPane body = buildBody();

        root.setTop(header);
        root.setCenter(body);

        return new Scene(root, 1200, 700);
    }

    private VBox buildHeader() {
        Label title = new Label("1.2 Thanh toán");
        title.setStyle("-fx-text-fill:#F5F0E1;-fx-font-size:30px;-fx-font-weight:bold;");

        Label subtitle = new Label("Tính tổng tiền • áp mã giảm giá • in hóa đơn • ghi nhận phương thức thanh toán");
        subtitle.setStyle("-fx-text-fill:#F2C57C;-fx-font-size:15px;");

        VBox header = new VBox(6, title, subtitle);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color:#6B4C3B;-fx-background-radius:18;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.35),18,0,0,6);");
        return header;
    }

    private BorderPane buildBody() {
        BorderPane body = new BorderPane();
        body.setPadding(new Insets(24, 0, 0, 0));

        VBox invoicePanel = buildInvoicePanel();
        VBox paymentPanel = buildPaymentPanel();

        body.setLeft(invoicePanel);
        BorderPane.setMargin(invoicePanel, new Insets(0, 20, 0, 0));
        body.setCenter(paymentPanel);

        return body;
    }

    private VBox buildInvoicePanel() {
        Label caption = new Label("Danh sách món");
        caption.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:20px;-fx-font-weight:bold;");

        TableView<InvoiceLine> table = new TableView<>(invoiceLines);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-radius:18;-fx-background-color:#FFFFFF;");

        TableColumn<InvoiceLine, String> nameCol = new TableColumn<>("Món");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<InvoiceLine, Integer> qtyCol = new TableColumn<>("SL");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setStyle("-fx-alignment:CENTER;");

        TableColumn<InvoiceLine, Double> priceCol = new TableColumn<>("Giá");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.setCellFactory(col -> new CurrencyCell());

        TableColumn<InvoiceLine, Double> totalCol = new TableColumn<>("Thành tiền");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
        totalCol.setCellFactory(col -> new CurrencyCell());

        table.getColumns().setAll(List.of(nameCol, qtyCol, priceCol, totalCol));

        VBox container = new VBox(16, caption, table);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:28;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.18),18,0,0,6);");
        container.setPrefWidth(520);
        VBox.setVgrow(table, Priority.ALWAYS);
        return container;
    }

    private VBox buildPaymentPanel() {
        Label caption = new Label("Chi tiết thanh toán");
        caption.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:20px;-fx-font-weight:bold;");

        subtotalLabel = createMetricLabel("Tạm tính", "0 đ");
        taxLabel = createMetricLabel("Thuế (8%)", "0 đ");
        serviceLabel = createMetricLabel("Phí dịch vụ (5%)", "0 đ");
        discountLabel = createMetricLabel("Giảm giá", "0 đ");
        totalLabel = createMetricLabel("Tổng cộng", "0 đ");
        totalLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:24px;-fx-font-weight:bold;");

        VBox summaryBox = new VBox(10, subtotalLabel, taxLabel, serviceLabel, discountLabel, totalLabel);
        summaryBox.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:18;-fx-padding:18;");

        discountField = new TextField();
        discountField.setPromptText("Nhập mã giảm giá");
        discountField.setStyle(controlStyle());

        Button applyDiscount = createPrimaryButton("Áp dụng mã");
        applyDiscount.setOnAction(e -> applyDiscountCode());

        Button removeDiscount = createGhostButton("Xoá mã");
        removeDiscount.setOnAction(e -> removeCoupon());

        HBox discountRow = new HBox(10, discountField, applyDiscount, removeDiscount);
        HBox.setHgrow(discountField, Priority.ALWAYS);

        Label paymentLabel = new Label("Phương thức thanh toán");
        paymentLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:16px;-fx-font-weight:bold;");

        paymentGroup = new ToggleGroup();

        RadioButton cashBtn = createPaymentRadio("Tiền mặt");
        RadioButton bankBtn = createPaymentRadio("Chuyển khoản");
        RadioButton walletBtn = createPaymentRadio("Ví điện tử");
        cashBtn.setSelected(true);

        VBox paymentOptions = new VBox(8, cashBtn, bankBtn, walletBtn);
        paymentOptions.setPadding(new Insets(12, 18, 12, 18));
        paymentOptions.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:16;");

        invoicePreview = new TextArea();
        invoicePreview.setEditable(false);
        invoicePreview.setPrefRowCount(12);
        invoicePreview.setStyle("-fx-background-color:#FAFAFA;-fx-border-color:rgba(107,76,59,0.2);"
                + "-fx-border-radius:14;-fx-background-radius:14;-fx-text-fill:#6B4C3B;");

        Button printBtn = createGhostButton("In hóa đơn");
        printBtn.setOnAction(e -> {
            // Vẫn cập nhật preview trong app
            refreshPreview();
            // Đồng thời mở file PDF hoá đơn từ backend (nếu có orderId hợp lệ)
            openInvoicePdfFromBackend();
        });

        Button labelBtn = createGhostButton("In tem món");
        labelBtn.setOnAction(e -> openItemLabelsFromBackend());

        Button completeBtn = createPrimaryButton("Hoàn tất thanh toán");
        completeBtn.setOnAction(e -> completePayment());

        Region spacer = new Region();
        HBox actionRow = new HBox(12, printBtn, labelBtn, spacer, completeBtn);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label previewLabel = new Label("Preview hóa đơn");
        previewLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:16px;-fx-font-weight:bold;");

        VBox container = new VBox(18,
                caption,
                summaryBox,
                discountRow,
                paymentLabel,
                paymentOptions,
                previewLabel,
                invoicePreview,
                actionRow);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:28;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.15),18,0,0,6);");
        VBox.setVgrow(invoicePreview, Priority.ALWAYS);
        return container;
    }

    private RadioButton createPaymentRadio(String text) {
        RadioButton radio = new RadioButton(text);
        radio.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:15px;");
        radio.setToggleGroup(paymentGroup);
        return radio;
    }

    private Label createMetricLabel(String title, String value) {
        Label label = new Label(title + ": " + value);
        label.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:16px;");
        return label;
    }

    private void applyDiscountCode() {
        String code = discountField.getText().trim();
        if (code.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Chưa nhập mã", "Vui lòng nhập mã giảm giá.");
            return;
        }
        if (orderId <= 0) {
            showAlert(Alert.AlertType.WARNING, "Chưa có order",
                    "Hãy tạo order cho bàn này trước khi áp dụng mã giảm giá.");
            return;
        }
        double subtotal = calculateSubtotal();

        // Gửi lên backend để validate + tính discount chính xác theo DB
        String safeCode = code.replace("\"", "\\\"");
        String jsonBody = String.format(Locale.US,
                "{\"code\":\"%s\",\"order_total\":%.0f}",
                safeCode, subtotal);

        HttpRequest request = HttpRequest.newBuilder(URI.create(VALIDATE_COUPON_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                System.out.println("validate-coupon => " +
                        response.statusCode() + " " + body);

                if (response.statusCode() != 200 || !body.contains("\"success\":true")) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                            "Mã không hợp lệ",
                            "Không áp dụng được mã: " + code));
                    return;
                }

                String couponIdStr = extractJsonNumber(body, "coupon_id");
                String discountStr = extractJsonNumber(body, "discount");
                if (couponIdStr.isBlank() || discountStr.isBlank()) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                            "Lỗi dữ liệu",
                            "Phản hồi từ server không hợp lệ."));
                    return;
                }

                int couponId = Integer.parseInt(couponIdStr);
                double discountValue = Double.parseDouble(discountStr);

                // Cập nhật UI trên JavaFX thread
                currentCouponId = couponId;
                Platform.runLater(() -> {
                    appliedDiscount = discountValue;
                    showAlert(Alert.AlertType.INFORMATION, "Áp dụng thành công",
                            "Đã áp dụng mã " + code + " với mức giảm " + currency.format(appliedDiscount));
                    refreshSummary();
                    refreshPreview();
                });

                // Lưu coupon vào Orders / OrderCoupons trên backend
                applyCouponToBackend(couponId, discountValue);

            } catch (Exception ex) {
                System.err.println("Lỗi gọi validate-coupon: " + ex.getMessage());
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                        "Lỗi server",
                        "Không thể kết nối đến server để kiểm tra mã giảm giá."));
            }
        }).start();
    }

    private void applyCouponToBackend(int couponId, double discountValue) {
        String jsonBody = String.format(
                "{\"order_id\":%d,\"coupon_id\":%d,\"discount\":%.0f}",
                orderId, couponId, discountValue);

        HttpRequest request = HttpRequest.newBuilder(URI.create(APPLY_COUPON_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("apply-coupon => " +
                        response.statusCode() + " " + response.body());
            } catch (Exception ex) {
                System.err.println("Lỗi gọi apply-coupon: " + ex.getMessage());
            }
        }).start();
    }

    private void removeCoupon() {
        // Nếu chưa có order_id hợp lệ thì chỉ xoá ở UI, không gọi backend
        if (orderId <= 0 || currentCouponId <= 0) {
            currentCouponId = -1;
            appliedDiscount = 0;
            discountField.clear();
            refreshSummary();
            refreshPreview();
            showAlert(Alert.AlertType.INFORMATION, "Đã xoá mã (UI)",
                    "Đã xoá mã giảm giá trên màn hình. Order hiện tại chưa có mã áp dụng trên server.");
            return;
        }

        String jsonBody = String.format(
                "{\"order_id\":%d,\"coupon_id\":%d}",
                orderId, currentCouponId);

        HttpRequest request = HttpRequest.newBuilder(URI.create(REMOVE_COUPON_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        new Thread(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("remove-coupon => " +
                        response.statusCode() + " " + response.body());

                if (response.statusCode() == 200 && response.body().contains("\"success\":true")) {
                    currentCouponId = -1;
                    appliedDiscount = 0;
                    Platform.runLater(() -> {
                        discountField.clear();
                        showAlert(Alert.AlertType.INFORMATION, "Đã xoá mã",
                                "Mã giảm giá đã được xoá khỏi order.");
                        refreshSummary();
                        refreshPreview();
                    });
                } else {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                            "Không xoá được mã",
                            "Server không thể xoá mã giảm giá. Vui lòng thử lại."));
                }
            } catch (Exception ex) {
                System.err.println("Lỗi gọi remove-coupon: " + ex.getMessage());
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                        "Lỗi server",
                        "Không thể kết nối đến server để xoá mã giảm giá."));
            }
        }).start();
    }

    private double calculateSubtotal() {
        return invoiceLines.stream().mapToDouble(InvoiceLine::getLineTotal).sum();
    }

    private void refreshSummary() {
        double subtotal = calculateSubtotal();
        double tax = subtotal * 0.08;
        double service = subtotal * 0.05;

        double total = subtotal + tax + service - appliedDiscount;
        if (total < 0) {
            total = 0;
        }

        subtotalLabel.setText("Tạm tính: " + currency.format(subtotal));
        taxLabel.setText("Thuế (8%): " + currency.format(tax));
        serviceLabel.setText("Phí dịch vụ (5%): " + currency.format(service));
        discountLabel.setText("Giảm giá: -" + currency.format(appliedDiscount));
        totalLabel.setText("Tổng cộng: " + currency.format(total));
    }

    private void refreshPreview() {
        StringBuilder builder = new StringBuilder();
        builder.append("Coffee Aura Receipt\n");
        builder.append("Bàn: ").append(tableLabel).append("\n");
        builder.append("Thời gian: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append("\n");
        builder.append("--------------------------------------------------\n");
        invoiceLines.forEach(line -> builder.append(String.format("%-25s x%-2d %s\n",
                line.getName(),
                line.getQuantity(),
                currency.format(line.getLineTotal()))));
        builder.append("--------------------------------------------------\n");
        builder.append(subtotalLabel.getText()).append("\n");
        builder.append(taxLabel.getText()).append("\n");
        builder.append(serviceLabel.getText()).append("\n");
        builder.append(discountLabel.getText()).append("\n");
        builder.append(totalLabel.getText()).append("\n");
        builder.append("Ghi chú: ").append(note == null || note.isBlank() ? "Không có" : note).append("\n\n");

        String method = ((RadioButton) paymentGroup.getSelectedToggle()).getText();
        builder.append("Phương thức: ").append(method).append("\n");
        builder.append("Nhân viên: ").append("Minh Quân").append("\n");

        invoicePreview.setText(builder.toString());
    }

    private void completePayment() {
        refreshSummary();
        refreshPreview();

        String method = ((RadioButton) paymentGroup.getSelectedToggle()).getText();
        double subtotal = calculateSubtotal();
        double tax = subtotal * 0.08;
        double service = subtotal * 0.05;
        double total = Math.max(0, subtotal + tax + service - appliedDiscount);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thanh toán thành công");
        alert.setHeaderText("Đã ghi nhận thanh toán bằng " + method);
        alert.setContentText(totalLabel.getText() + "\n"
                + "Chi tiết hóa đơn đã sẵn sàng để in.");
        alert.showAndWait();

        if (listener != null) {
            listener.onCompleted(tableLabel, total, method, LocalDateTime.now());
        }
        if (window != null) {
            window.close();
        }
    }

    private void openInvoicePdfFromBackend() {
        if (orderId <= 0) {
            showAlert(Alert.AlertType.WARNING, "Không thể in hoá đơn",
                    "Chưa có order_id từ backend. Hãy chắc chắn đã tạo order trước khi in.");
            return;
        }
        try {
            String url = INVOICE_PDF_URL + "?order_id=" + orderId;
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Mở hoá đơn thủ công",
                        "Máy không hỗ trợ mở trình duyệt tự động. Hãy mở URL:\n" + url);
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Lỗi in hoá đơn",
                    "Không mở được hoá đơn PDF từ backend:\n" + ex.getMessage());
        }
    }

    private void openItemLabelsFromBackend() {
        if (orderId <= 0) {
            showAlert(Alert.AlertType.WARNING, "Không thể in tem món",
                    "Chưa có order_id từ backend. Hãy chắc chắn đã tạo order trước khi in tem.");
            return;
        }
        try {
            String url = ITEM_LABELS_PDF_URL + "?order_id=" + orderId;
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Mở tem món thủ công",
                        "Máy không hỗ trợ mở trình duyệt tự động. Hãy mở URL:\n" + url);
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Lỗi in tem món",
                    "Không mở được tem món PDF từ backend:\n" + ex.getMessage());
        }
    }

    // Helper rất đơn giản để lấy số từ JSON dạng "key":123 hoặc "key":123.45
    private String extractJsonNumber(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start < 0) {
            return "";
        }
        start += pattern.length();
        int end = start;
        while (end < json.length() && "0123456789.-".indexOf(json.charAt(end)) >= 0) {
            end++;
        }
        return json.substring(start, end).trim();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:#F2C57C;-fx-background-radius:14;"
                + "-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-padding:12 20;");
        return button;
    }

    private Button createGhostButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:transparent;-fx-border-color:rgba(107,76,59,0.4);"
                + "-fx-border-radius:14;-fx-text-fill:#6B4C3B;-fx-padding:10 18;");
        return button;
    }

    private String controlStyle() {
        return "-fx-background-radius:14;-fx-background-color:#FAFAFA;"
                + "-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;"
                + "-fx-text-fill:#6B4C3B;-fx-prompt-text-fill:rgba(107,76,59,0.5);"
                + "-fx-padding:12;";
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class InvoiceLine {
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty unitPrice;

        public InvoiceLine(String name, int quantity, double unitPrice) {
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.unitPrice = new SimpleDoubleProperty(unitPrice);
        }

        public String getName() {
            return name.get();
        }

        public int getQuantity() {
            return quantity.get();
        }

        public double getUnitPrice() {
            return unitPrice.get();
        }

        public double getLineTotal() {
            return getQuantity() * getUnitPrice();
        }
    }

    private class CurrencyCell extends javafx.scene.control.TableCell<InvoiceLine, Double> {
        @Override
        protected void updateItem(Double value, boolean empty) {
            super.updateItem(value, empty);
            if (empty || value == null) {
                setText(null);
            } else {
                setText(currency.format(value));
                setStyle("-fx-text-fill:#6B4C3B;");
            }
        }
    }
}
