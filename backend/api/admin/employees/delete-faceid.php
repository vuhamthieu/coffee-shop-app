<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['employee_id'])) {
    echo json_encode(["error" => "employee_id is required"]);
    exit;
}

$sql = "DELETE FROM FaceData WHERE employee_id = :employee_id";
$stmt = $pdo->prepare($sql);
$stmt->execute([':employee_id' => $data['employee_id']]);

echo json_encode([
    "success" => true,
    "message" => "Face ID data deleted"
]);
