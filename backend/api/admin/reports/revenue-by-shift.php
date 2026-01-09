<?php
include __DIR__ . '/../../../connect.php';

$date = $_GET['date'] ?? date('Y-m-d');

$sql = "SELECT 
        HOUR(created_at) as hour,
        SUM(total) as total_revenue,
        COUNT(id) as order_count
        FROM Orders
        WHERE DATE(created_at) = :date AND order_status = 'paid'
        GROUP BY HOUR(created_at)
        ORDER BY hour ASC";

$stmt = $pdo->prepare($sql);
$stmt->execute([':date' => $date]);

$data = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Create shift groups: Morning (6-11), Afternoon (12-17), Evening (18-23), Night (0-5)
$shifts = [
    'morning' => ['start' => 6, 'end' => 11, 'revenue' => 0, 'orders' => 0],
    'afternoon' => ['start' => 12, 'end' => 17, 'revenue' => 0, 'orders' => 0],
    'evening' => ['start' => 18, 'end' => 23, 'revenue' => 0, 'orders' => 0],
    'night' => ['start' => 0, 'end' => 5, 'revenue' => 0, 'orders' => 0]
];

foreach ($data as $row) {
    $hour = $row['hour'];
    if ($hour >= 6 && $hour <= 11) {
        $shifts['morning']['revenue'] += $row['total_revenue'];
        $shifts['morning']['orders'] += $row['order_count'];
    } elseif ($hour >= 12 && $hour <= 17) {
        $shifts['afternoon']['revenue'] += $row['total_revenue'];
        $shifts['afternoon']['orders'] += $row['order_count'];
    } elseif ($hour >= 18 && $hour <= 23) {
        $shifts['evening']['revenue'] += $row['total_revenue'];
        $shifts['evening']['orders'] += $row['order_count'];
    } else {
        $shifts['night']['revenue'] += $row['total_revenue'];
        $shifts['night']['orders'] += $row['order_count'];
    }
}

echo json_encode([
    "success" => true,
    "date" => $date,
    "shifts" => $shifts
]);
