<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['employee_id']) || empty($data['role_id'])) {
    echo json_encode(["error" => "employee_id and role_id are required"]);
    exit;
}

$sql = "UPDATE Employees SET role_id = :role_id WHERE id = :id";
$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':role_id' => $data['role_id'],
    ':id'      => $data['employee_id']
]);

echo json_encode([
    "success" => true,
    "message" => "Employee role updated"
]);
