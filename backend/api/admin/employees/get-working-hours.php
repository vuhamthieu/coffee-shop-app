<?php
include __DIR__ . '/../../../connect.php';

if (empty($_GET['employee_id'])) {
    echo json_encode(["error" => "employee_id is required"]);
    exit;
}

$employee_id = $_GET['employee_id'];
$month = $_GET['month'] ?? date('m');
$year = $_GET['year'] ?? date('Y');

$sql = "SELECT a.*, e.employee_name 
        FROM Attendance a
        JOIN Employees e ON a.employee_id = e.id
        WHERE a.employee_id = :employee_id
        AND MONTH(a.checkin_time) = :month
        AND YEAR(a.checkin_time) = :year
        ORDER BY a.checkin_time DESC";

$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':employee_id' => $employee_id,
    ':month' => $month,
    ':year' => $year
]);

$attendances = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Calculate total hours
$total_hours = 0;
foreach ($attendances as $record) {
    if ($record['checkout_time']) {
        $checkin = new DateTime($record['checkin_time']);
        $checkout = new DateTime($record['checkout_time']);
        $diff = $checkout->diff($checkin);
        $hours = $diff->h + ($diff->i / 60);
        $total_hours += $hours;
    }
}

echo json_encode([
    "success" => true,
    "data" => $attendances,
    "total_hours" => round($total_hours, 2)
]);
