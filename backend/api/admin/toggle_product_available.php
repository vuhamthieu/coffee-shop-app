<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['id']) || !isset($data['available'])) {
    echo json_encode(["error" => "id and available are required"]);
    exit;
}

$sql = "UPDATE Products SET available = :available WHERE id = :id";
$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':available' => $data['available'] ? 1 : 0,
    ':id' => $data['id']
]);

echo json_encode(["success" => true, "message" => "Product availability updated"]);
