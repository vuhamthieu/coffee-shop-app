<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['id'])) {
    echo json_encode(["error" => "id is required"]);
    exit;
}

$id = $data['id'];
$updates = [];
$params = [':id' => $id];

// Allow updating these fields
$allowed = ['employee_name', 'phone', 'username', 'role_id', 'active'];

foreach ($allowed as $field) {
    if (array_key_exists($field, $data)) {
        $updates[] = "$field = :$field";
        $params[":$field"] = $data[$field];
    }
}

// Handle password separately
if (isset($data['employee_password']) && !empty($data['employee_password'])) {
    $updates[] = "employee_password = :employee_password";
    $params[':employee_password'] = password_hash($data['employee_password'], PASSWORD_DEFAULT);
}

if (empty($updates)) {
    echo json_encode(["error" => "No fields to update"]);
    exit;
}

// Check username uniqueness if updating username
if (isset($data['username'])) {
    $checkSql = "SELECT id FROM Employees WHERE username = :username AND id != :id";
    $checkStmt = $pdo->prepare($checkSql);
    $checkStmt->execute([':username' => $data['username'], ':id' => $id]);
    if ($checkStmt->fetch()) {
        echo json_encode(["error" => "Username already exists"]);
        exit;
    }
}

$sql = "UPDATE Employees SET " . implode(", ", $updates) . " WHERE id = :id";
$stmt = $pdo->prepare($sql);
$stmt->execute($params);

echo json_encode(["success" => true, "message" => "Employee updated"]);
