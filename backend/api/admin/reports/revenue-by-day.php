<?php
include __DIR__ . '/../../../connect.php';

$date = $_GET['date'] ?? date('Y-m-d');

$sql = "SELECT SUM(total) as total_revenue, COUNT(id) as order_count, COUNT(DISTINCT table_id) as customer_count
        FROM Orders 
        WHERE DATE(created_at) = :date AND order_status = 'paid'";

$stmt = $pdo->prepare($sql);
$stmt->execute([':date' => $date]);
$revenue = $stmt->fetch(PDO::FETCH_ASSOC);

echo json_encode([
    "success" => true,
    "date" => $date,
    "total_revenue" => $revenue['total_revenue'] ?? 0,
    "order_count" => $revenue['order_count'] ?? 0,
    "customer_count" => $revenue['customer_count'] ?? 0
]);
