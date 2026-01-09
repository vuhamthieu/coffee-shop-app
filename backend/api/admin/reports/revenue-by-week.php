<?php
include __DIR__ . '/../../../connect.php';

$year = $_GET['year'] ?? date('Y');
$week = $_GET['week'] ?? date('W');

$sql = "SELECT 
        DATE(created_at) as date,
        SUM(total) as total_revenue,
        COUNT(id) as order_count
        FROM Orders
        WHERE YEAR(created_at) = :year 
        AND WEEK(created_at) = :week 
        AND order_status = 'paid'
        GROUP BY DATE(created_at)
        ORDER BY created_at ASC";

$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':year' => $year,
    ':week' => $week
]);

$data = $stmt->fetchAll(PDO::FETCH_ASSOC);

$total = 0;
foreach ($data as $row) {
    $total += $row['total_revenue'];
}

echo json_encode([
    "success" => true,
    "year" => $year,
    "week" => $week,
    "data" => $data,
    "total_revenue" => $total
]);
