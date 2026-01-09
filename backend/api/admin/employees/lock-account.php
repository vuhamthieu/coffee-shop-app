<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['employee_id'])) {
    echo json_encode(["error" => "employee_id is required"]);
    exit;
}

$sql = "UPDATE Employees SET active = 0 WHERE id = :id";
$stmt = $pdo->prepare($sql);
$stmt->execute([':id' => $data['employee_id']]);

echo json_encode([
    "success" => true,
    "message" => "Account locked"
]);
