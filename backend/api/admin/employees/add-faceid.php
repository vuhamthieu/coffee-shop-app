<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['employee_id']) || empty($data['embedding'])) {
    echo json_encode(["error" => "employee_id and embedding are required"]);
    exit;
}

// Check if employee exists
$checkSql = "SELECT id FROM Employees WHERE id = :id";
$checkStmt = $pdo->prepare($checkSql);
$checkStmt->execute([':id' => $data['employee_id']]);

if (!$checkStmt->fetch()) {
    echo json_encode(["error" => "Employee not found"]);
    exit;
}

// Delete old face data if exists
$deleteSql = "DELETE FROM FaceData WHERE employee_id = :employee_id";
$deleteStmt = $pdo->prepare($deleteSql);
$deleteStmt->execute([':employee_id' => $data['employee_id']]);

// Insert new face embedding
$sql = "INSERT INTO FaceData (employee_id, embedding) VALUES (:employee_id, :embedding)";
$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':employee_id' => $data['employee_id'],
    ':embedding' => $data['embedding']  // Base64 encoded or binary data
]);

echo json_encode([
    "success" => true,
    "message" => "Face ID registered successfully",
    "face_id" => $pdo->lastInsertId()
]);
