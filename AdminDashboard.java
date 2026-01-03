import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AdminDashboard extends Application {

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private final ObservableList<PaymentRecord> allPayments = FXCollections.observableArrayList();
    private final FilteredList<PaymentRecord> filteredPayments = new FilteredList<>(allPayments);
    private final SortedList<PaymentRecord> sortedPayments = new SortedList<>(filteredPayments);

    // UI Components
    private Label totalRevenueLabel;
    private Label todayRevenueLabel;
    private Label weeklyRevenueLabel;
    private Label monthlyRevenueLabel;
    private Label cashLabel;
    private Label cardLabel;
    private Label qrLabel;
    private Label transferLabel;
    private TableView<PaymentRecord> table;
    private TextField searchField;
    private ComboBox<String> filterMethodCombo;
    private ComboBox<String> dateRangeCombo;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20, 24, 20, 24));
        root.setStyle("-fx-background-color:#F5F0E1;-fx-font-family:'Segoe UI',sans-serif;");

        // Header
        VBox header = buildHeader();
        root.setTop(header);

        // Statistics Cards
        FlowPane statsCards = buildStatsCards();
        root.setCenter(buildMainContent(statsCards));

        Scene scene = new Scene(root, 1200, 750);
        primaryStage.setTitle("Coffee Aura - Admin Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial load - data will come from database
        loadSampleData();
    }

    private VBox buildHeader() {
        Label title = new Label("Admin Dashboard • Dòng tiền");
        title.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:24px;-fx-font-weight:bold;");

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().add(title);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        return new VBox(headerBox);
    }

    private FlowPane buildStatsCards() {
        FlowPane cards = new FlowPane();
        cards.setHgap(15);
        cards.setVgap(15);
        cards.setPadding(new Insets(0, 0, 20, 0));

        // Total Revenue Card
        VBox totalCard = createStatCard("Tổng doanh thu", "0", "#6B4C3B");
        totalRevenueLabel = (Label) totalCard.getChildren().get(1);

        // Today Revenue Card
        VBox todayCard = createStatCard("Hôm nay", "0", "#C08A64");
        todayRevenueLabel = (Label) todayCard.getChildren().get(1);

        // Weekly Revenue Card
        VBox weeklyCard = createStatCard("Tuần này", "0", "#F2C57C");
        weeklyRevenueLabel = (Label) weeklyCard.getChildren().get(1);

        // Monthly Revenue Card
        VBox monthlyCard = createStatCard("Tháng này", "0", "#6B4C3B");
        monthlyRevenueLabel = (Label) monthlyCard.getChildren().get(1);

        // Payment Method Cards - Separate cards for each method
        VBox cashCard = createStatCard("💵 Tiền mặt", "0", "#6B4C3B");
        cashLabel = (Label) cashCard.getChildren().get(1);

        VBox cardCard = createStatCard("💳 Thẻ", "0", "#C08A64");
        cardLabel = (Label) cardCard.getChildren().get(1);

        VBox qrCard = createStatCard("📱 QR", "0", "#F2C57C");
        qrLabel = (Label) qrCard.getChildren().get(1);

        VBox transferCard = createStatCard("🏦 Chuyển khoản", "0", "#6B4C3B");
        transferLabel = (Label) transferCard.getChildren().get(1);

        cards.getChildren().addAll(totalCard, todayCard, weeklyCard, monthlyCard,
                cashCard, cardCard, qrCard, transferCard);
        return cards;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:18;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.12),14,0,0,4);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:14px;-fx-font-weight:bold;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill:" + color + ";-fx-font-size:20px;-fx-font-weight:bold;");
        valueLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private VBox buildMainContent(FlowPane statsCards) {
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(0));

        // Controls Panel
        HBox controlsPanel = buildControlsPanel();

        // Table Panel
        VBox tablePanel = buildTablePanel();

        mainContent.getChildren().addAll(statsCards, controlsPanel, tablePanel);
        VBox.setVgrow(tablePanel, Priority.ALWAYS);

        return mainContent;
    }

    private HBox buildControlsPanel() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 10, 0));

        // Search Field
        Label searchLabel = new Label("Tìm kiếm:");
        searchLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;");
        searchField = new TextField();
        searchField.setPromptText("Tìm theo bàn, phương thức...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-radius:12;-fx-background-color:#FAFAFA;"
                + "-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;"
                + "-fx-padding:10;-fx-text-fill:#6B4C3B;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Filter by Payment Method
        Label methodLabel = new Label("Phương thức:");
        methodLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;");
        filterMethodCombo = new ComboBox<>();
        filterMethodCombo.getItems().addAll("Tất cả", "Tiền mặt", "Thẻ", "QR", "Chuyển khoản");
        filterMethodCombo.setValue("Tất cả");
        filterMethodCombo.setPrefWidth(150);
        filterMethodCombo.setStyle("-fx-background-radius:12;-fx-background-color:#FAFAFA;"
                + "-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;"
                + "-fx-padding:10;-fx-text-fill:#6B4C3B;");
        filterMethodCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Date Range Filter
        Label dateLabel = new Label("Khoảng thời gian:");
        dateLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;");
        dateRangeCombo = new ComboBox<>();
        dateRangeCombo.getItems().addAll("Tất cả", "Hôm nay", "Tuần này", "Tháng này", "7 ngày qua", "30 ngày qua");
        dateRangeCombo.setValue("Tất cả");
        dateRangeCombo.setPrefWidth(150);
        dateRangeCombo.setStyle("-fx-background-radius:12;-fx-background-color:#FAFAFA;"
                + "-fx-border-color:rgba(107,76,59,0.2);-fx-border-width:1;"
                + "-fx-padding:10;-fx-text-fill:#6B4C3B;");
        dateRangeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
            updateStatistics();
        });

        // Buttons
        Button refreshBtn = createPrimaryButton("🔄 Làm mới");
        refreshBtn.setOnAction(e -> {
            loadSampleData();
            updateStatistics();
        });

        Button exportBtn = createGhostButton("📊 Xuất dữ liệu");
        exportBtn.setOnAction(e -> exportData());

        HBox buttonBox = new HBox(10, refreshBtn, exportBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);

        controls.getChildren().addAll(searchLabel, searchField, methodLabel, filterMethodCombo,
                dateLabel, dateRangeCombo, buttonBox);

        return controls;
    }

    private VBox buildTablePanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:18;"
                + "-fx-effect:dropshadow(gaussian,rgba(107,76,59,0.12),14,0,0,4);");

        Label tableTitle = new Label("Lịch sử thanh toán");
        tableTitle.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:18px;-fx-font-weight:bold;");

        table = new TableView<>(sortedPayments);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Chưa có dữ liệu thanh toán"));
        table.setStyle("-fx-background-color:#FFFFFF;-fx-background-radius:12;"
                + "-fx-border-color:rgba(107,76,59,0.1);-fx-border-width:1;");

        // Configure sorted list
        sortedPayments.comparatorProperty().bind(table.comparatorProperty());

        // Date Column
        TableColumn<PaymentRecord, String> colDate = new TableColumn<>("Thời gian");
        colDate.setCellValueFactory(new PropertyValueFactory<>("time"));
        colDate.setPrefWidth(180);
        colDate.setStyle("-fx-alignment:CENTER_LEFT;");

        // Table Column
        TableColumn<PaymentRecord, String> colTable = new TableColumn<>("Bàn");
        colTable.setCellValueFactory(new PropertyValueFactory<>("table"));
        colTable.setPrefWidth(120);
        colTable.setStyle("-fx-alignment:CENTER;");

        // Payment Method Column
        TableColumn<PaymentRecord, String> colMethod = new TableColumn<>("Phương thức");
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colMethod.setPrefWidth(140);
        colMethod.setStyle("-fx-alignment:CENTER;");

        // Amount Column with custom cell factory for currency formatting
        TableColumn<PaymentRecord, Double> colAmount = new TableColumn<>("Số tiền");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setPrefWidth(160);
        colAmount.setCellFactory(column -> new javafx.scene.control.TableCell<PaymentRecord, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currency.format(amount));
                    setStyle("-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-alignment:CENTER_RIGHT;");
                }
            }
        });

        table.getColumns().addAll(colDate, colTable, colMethod, colAmount);

        // Summary row
        Label summaryLabel = new Label();
        summaryLabel.setStyle("-fx-text-fill:#6B4C3B;-fx-font-size:14px;-fx-font-weight:bold;");
        updateSummaryLabel(summaryLabel);

        // Update summary when filtered list changes
        filteredPayments.addListener((javafx.collections.ListChangeListener.Change<? extends PaymentRecord> c) -> {
            updateSummaryLabel(summaryLabel);
        });

        panel.getChildren().addAll(tableTitle, table, summaryLabel);
        VBox.setVgrow(table, Priority.ALWAYS);

        return panel;
    }

    private void updateSummaryLabel(Label label) {
        double filteredTotal = filteredPayments.stream()
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        int count = filteredPayments.size();
        label.setText(String.format("Tổng: %s • %d giao dịch", currency.format(filteredTotal), count));
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String methodFilter = filterMethodCombo.getValue();
        String dateFilter = dateRangeCombo.getValue();

        filteredPayments.setPredicate(payment -> {
            // Search filter
            boolean matchesSearch = searchText.isEmpty() ||
                    payment.getTable().toLowerCase().contains(searchText) ||
                    payment.getMethod().toLowerCase().contains(searchText);

            // Method filter
            boolean matchesMethod = methodFilter == null || methodFilter.equals("Tất cả") ||
                    payment.getMethod().equals(methodFilter);

            // Date filter
            boolean matchesDate = true;
            if (dateFilter != null && !dateFilter.equals("Tất cả")) {
                LocalDate paymentDate = LocalDate.parse(payment.getTime().substring(0, 10),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate today = LocalDate.now();

                switch (dateFilter) {
                    case "Hôm nay":
                        matchesDate = paymentDate.equals(today);
                        break;
                    case "Tuần này":
                        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                        matchesDate = !paymentDate.isBefore(weekStart);
                        break;
                    case "Tháng này":
                        matchesDate = paymentDate.getMonth() == today.getMonth() &&
                                paymentDate.getYear() == today.getYear();
                        break;
                    case "7 ngày qua":
                        matchesDate = !paymentDate.isBefore(today.minusDays(7));
                        break;
                    case "30 ngày qua":
                        matchesDate = !paymentDate.isBefore(today.minusDays(30));
                        break;
                }
            }

            return matchesSearch && matchesMethod && matchesDate;
        });
    }

    private void updateStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate monthStart = today.withDayOfMonth(1);

        double total = allPayments.stream().mapToDouble(PaymentRecord::getAmount).sum();
        double todayRevenue = allPayments.stream()
                .filter(p -> {
                    LocalDate pDate = LocalDate.parse(p.getTime().substring(0, 10),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return pDate.equals(today);
                })
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        double weeklyRevenue = allPayments.stream()
                .filter(p -> {
                    LocalDate pDate = LocalDate.parse(p.getTime().substring(0, 10),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return !pDate.isBefore(weekStart);
                })
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        double monthlyRevenue = allPayments.stream()
                .filter(p -> {
                    LocalDate pDate = LocalDate.parse(p.getTime().substring(0, 10),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return !pDate.isBefore(monthStart);
                })
                .mapToDouble(PaymentRecord::getAmount)
                .sum();

        totalRevenueLabel.setText(currency.format(total));
        todayRevenueLabel.setText(currency.format(todayRevenue));
        weeklyRevenueLabel.setText(currency.format(weeklyRevenue));
        monthlyRevenueLabel.setText(currency.format(monthlyRevenue));

        // Payment method breakdown - calculate revenue for each method
        double cashRevenue = allPayments.stream()
                .filter(p -> p.getMethod().equals("Tiền mặt"))
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        long cashCount = allPayments.stream().filter(p -> p.getMethod().equals("Tiền mặt")).count();

        double cardRevenue = allPayments.stream()
                .filter(p -> p.getMethod().equals("Thẻ"))
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        long cardCount = allPayments.stream().filter(p -> p.getMethod().equals("Thẻ")).count();

        double qrRevenue = allPayments.stream()
                .filter(p -> p.getMethod().equals("QR"))
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        long qrCount = allPayments.stream().filter(p -> p.getMethod().equals("QR")).count();

        double transferRevenue = allPayments.stream()
                .filter(p -> p.getMethod().equals("Chuyển khoản"))
                .mapToDouble(PaymentRecord::getAmount)
                .sum();
        long transferCount = allPayments.stream().filter(p -> p.getMethod().equals("Chuyển khoản")).count();

        // Update individual payment method cards
        cashLabel.setText(currency.format(cashRevenue) + "\n(" + cashCount + " giao dịch)");
        cardLabel.setText(currency.format(cardRevenue) + "\n(" + cardCount + " giao dịch)");
        qrLabel.setText(currency.format(qrRevenue) + "\n(" + qrCount + " giao dịch)");
        transferLabel.setText(currency.format(transferRevenue) + "\n(" + transferCount + " giao dịch)");
    }

    private void loadSampleData() {
        // Clear all data - ready for database integration
        allPayments.clear();
        updateStatistics();
    }

    private void exportData() {
        // Simple export functionality - in real app, you'd export to CSV/Excel
        StringBuilder sb = new StringBuilder();
        sb.append("Thời gian,Bàn,Phương thức,Số tiền\n");
        for (PaymentRecord p : filteredPayments) {
            sb.append(String.format("%s,%s,%s,%.0f\n",
                    p.getTime(), p.getTable(), p.getMethod(), p.getAmount()));
        }
        System.out.println("=== EXPORT DATA ===");
        System.out.println(sb.toString());
        System.out.println("===================");
        // In a real implementation, you could save to file or show a dialog
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:#F2C57C;-fx-background-radius:14;"
                + "-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-padding:12 18;"
                + "-fx-cursor:hand;");
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color:#E8B86A;-fx-background-radius:14;"
                        + "-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-padding:12 18;"
                        + "-fx-cursor:hand;"));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color:#F2C57C;-fx-background-radius:14;"
                        + "-fx-text-fill:#6B4C3B;-fx-font-weight:bold;-fx-padding:12 18;"
                        + "-fx-cursor:hand;"));
        return button;
    }

    private Button createGhostButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:transparent;-fx-border-color:rgba(107,76,59,0.4);"
                + "-fx-border-radius:14;-fx-text-fill:#6B4C3B;-fx-padding:10 16;"
                + "-fx-cursor:hand;");
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color:rgba(107,76,59,0.1);-fx-border-color:rgba(107,76,59,0.6);"
                        + "-fx-border-radius:14;-fx-text-fill:#6B4C3B;-fx-padding:10 16;"
                        + "-fx-cursor:hand;"));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color:transparent;-fx-border-color:rgba(107,76,59,0.4);"
                        + "-fx-border-radius:14;-fx-text-fill:#6B4C3B;-fx-padding:10 16;"
                        + "-fx-cursor:hand;"));
        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class PaymentRecord {
        private final String time;
        private final String table;
        private final double amount;
        private final String method;

        public PaymentRecord(String time, String table, double amount, String method) {
            this.time = time;
            this.table = table;
            this.amount = amount;
            this.method = method;
        }

        public String getTime() {
            return time;
        }

        public String getTable() {
            return table;
        }

        public double getAmount() {
            return amount;
        }

        public String getMethod() {
            return method;
        }
    }
}
