<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['employee_name']) || empty($data['username']) || empty($data['employee_password']) || empty($data['role_id'])) {
    echo json_encode(["error" => "employee_name, username, employee_password, and role_id are required"]);
    exit;
}

// Check if username already exists
$checkSql = "SELECT id FROM Employees WHERE username = :username";
$checkStmt = $pdo->prepare($checkSql);
$checkStmt->execute([':username' => $data['username']]);

if ($checkStmt->fetch()) {
    echo json_encode(["error" => "Username already exists"]);
    exit;
}

$sql = "INSERT INTO Employees (employee_name, phone, username, employee_password, role_id, active)
        VALUES (:employee_name, :phone, :username, :employee_password, :role_id, 1)";

$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':employee_name' => $data['employee_name'],
    ':phone' => $data['phone'] ?? null,
    ':username' => $data['username'],
    ':employee_password' => password_hash($data['employee_password'], PASSWORD_DEFAULT),
    ':role_id' => $data['role_id']
]);

echo json_encode([
    "success" => true,
    "message" => "Employee added",
    "id" => $pdo->lastInsertId()
]);

