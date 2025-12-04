<?php

include __DIR__ . '/../../connect.php';

// --- Kiểm tra phương thức ---
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        "status" => false,
        "message" => "Method not allowed"
    ]);
    exit;
}

// --- Nhận dữ liệu ---
$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['employee_id']) || empty($data['employee_id'])) {
    echo json_encode([
        "status" => false,
        "message" => "employee_id is required"
    ]);
    exit;
}

$employee_id = $conn->real_escape_string($data['employee_id']);

// --- Kiểm tra tồn tại nhân viên ---
$sql = "SELECT id FROM Employees WHERE id = $employee_id AND active = 1";
$result = $conn->query($sql);

if ($result->num_rows == 0) {
    echo json_encode([
        "status" => false,
        "message" => "Employee not found or inactive"
    ]);
    exit;
}

// --- Lấy bản ghi check-in chưa checkout ---
$sql = "
    SELECT id, checkin_time 
    FROM Attendance 
    WHERE employee_id = $employee_id 
      AND checkout_time IS NULL
    ORDER BY checkin_time DESC
    LIMIT 1
";

$result = $conn->query($sql);

if ($result->num_rows == 0) {
    echo json_encode([
        "status" => false,
        "message" => "No active shift found (already checked out or not checked in)"
    ]);
    exit;
}

$row = $result->fetch_assoc();
$attendance_id = $row['id'];
$checkin_time  = $row['checkin_time'];

// --- Tính tổng giờ làm ---
$checkout_time = date("Y-m-d H:i:s");

$start = strtotime($checkin_time);
$end   = strtotime($checkout_time);

$total_hours = round(($end - $start) / 3600, 2); // 2 số lẻ

// --- Cập nhật checkout ---
$sql = "
    UPDATE Attendance 
    SET checkout_time = '$checkout_time',
        total_hours = $total_hours
    WHERE id = $attendance_id
";

if ($conn->query($sql)) {

    // Ghi log
    $conn->query("
        INSERT INTO ActivityLog (employee_id, action_name, detail)
        VALUES ($employee_id, 'CHECKOUT', 'Checkout at $checkout_time, total hours = $total_hours')
    ");

    echo json_encode([
        "status" => true,
        "message" => "Checkout success",
        "data" => [
            "employee_id" => $employee_id,
            "checkin_time" => $checkin_time,
            "checkout_time" => $checkout_time,
            "total_hours" => $total_hours
        ]
    ]);

} else {
    echo json_encode([
        "status" => false,
        "message" => "Database update failed"
    ]);
}

$conn->close();
?>
