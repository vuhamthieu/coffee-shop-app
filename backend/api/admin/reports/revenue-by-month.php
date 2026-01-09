<?php
include __DIR__ . '/../../../connect.php';

$year = $_GET['year'] ?? date('Y');
$month = $_GET['month'] ?? date('m');

$sql = "SELECT 
        DATE_FORMAT(created_at, '%Y-%m-%d') as date,
        SUM(total) as total_revenue,
        COUNT(id) as order_count
        FROM Orders
        WHERE YEAR(created_at) = :year 
        AND MONTH(created_at) = :month 
        AND order_status = 'paid'
        GROUP BY DATE(created_at)
        ORDER BY created_at ASC";

$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':year' => $year,
    ':month' => $month
]);

$data = $stmt->fetchAll(PDO::FETCH_ASSOC);

$total = 0;
foreach ($data as $row) {
    $total += $row['total_revenue'];
}

echo json_encode([
    "success" => true,
    "year" => $year,
    "month" => $month,
    "data" => $data,
    "total_revenue" => $total
]);
