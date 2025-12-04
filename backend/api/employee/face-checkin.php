<?php
include __DIR__ . '/../../connect.php';

// 1. Check POST method
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["status" => false, "message" => "Invalid request method"]);
    exit;
}

// 2. Get employee_id from POST
$employee_id = $_POST['employee_id'] ?? null;

if (!$employee_id) {
    echo json_encode(["status" => false, "message" => "employee_id is required"]);
    exit;
}

try {
    // 3. Check if employee exists and is active
    $stmt = $pdo->prepare("SELECT id, active FROM Employees WHERE id = ?");
    $stmt->execute([$employee_id]);
    $employee = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$employee) {
        echo json_encode(["status" => false, "message" => "Employee not found"]);
        exit;
    }

    if ($employee['active'] == 0) {
        echo json_encode(["status" => false, "message" => "Employee is deactivated"]);
        exit;
    }

    // 4. Check if employee already has an open shift (has checkin but no checkout)
    $stmt = $pdo->prepare("
        SELECT id FROM Attendance 
        WHERE employee_id = ? 
          AND checkout_time IS NULL
        ORDER BY id DESC LIMIT 1
    ");
    $stmt->execute([$employee_id]);
    $openShift = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($openShift) {
        echo json_encode(["status" => false, "message" => "Already checked in"]);
        exit;
    }

    // 5. Insert new attendance record
    $stmt = $pdo->prepare("
        INSERT INTO Attendance (employee_id, checkin_time)
        VALUES (?, NOW())
    ");
    $stmt->execute([$employee_id]);

    // 6. Log activity
    $log = $pdo->prepare("
        INSERT INTO ActivityLog (employee_id, action_name, detail)
        VALUES (?, 'checkin', 'Employee checked in by face ID')
    ");
    $log->execute([$employee_id]);

    echo json_encode([
        "status" => true,
        "message" => "Check-in successful",
        "attendance_id" => $pdo->lastInsertId(),
        "checkin_time" => date("Y-m-d H:i:s")
    ]);
} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Server error",
        "error" => $e->getMessage()
    ]);
}
?>
