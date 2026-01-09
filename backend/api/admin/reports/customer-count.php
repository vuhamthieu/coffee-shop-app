<?php
include __DIR__ . '/../../../connect.php';

$year = $_GET['year'] ?? date('Y');
$month = $_GET['month'] ?? date('m');

$sql = "SELECT 
        COUNT(DISTINCT table_id) as customer_count,
        COUNT(DISTINCT id) as total_orders,
        SUM(total) as total_revenue
        FROM Orders
        WHERE YEAR(created_at) = :year 
        AND MONTH(created_at) = :month
        AND order_status = 'paid'";

$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':year' => $year,
    ':month' => $month
]);

$stats = $stmt->fetch(PDO::FETCH_ASSOC);

// Get daily distribution
$dailySql = "SELECT 
            DATE(created_at) as date,
            COUNT(DISTINCT table_id) as daily_customers
            FROM Orders
            WHERE YEAR(created_at) = :year 
            AND MONTH(created_at) = :month
            AND order_status = 'paid'
            GROUP BY DATE(created_at)
            ORDER BY created_at ASC";

$dailyStmt = $pdo->prepare($dailySql);
$dailyStmt->execute([
    ':year' => $year,
    ':month' => $month
]);

$dailyData = $dailyStmt->fetchAll(PDO::FETCH_ASSOC);

echo json_encode([
    "success" => true,
    "year" => $year,
    "month" => $month,
    "total_customers" => $stats['customer_count'] ?? 0,
    "total_orders" => $stats['total_orders'] ?? 0,
    "total_revenue" => $stats['total_revenue'] ?? 0,
    "daily_data" => $dailyData
]);
