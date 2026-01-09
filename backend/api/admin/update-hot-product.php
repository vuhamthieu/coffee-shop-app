<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['id']) || !isset($data['is_hot'])) {
    echo json_encode(["error" => "id and is_hot are required"]);
    exit;
}

$sql = "UPDATE Products SET is_hot = :is_hot WHERE id = :id";
$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':is_hot' => $data['is_hot'] ? 1 : 0,
    ':id' => $data['id']
]);

echo json_encode(["success" => true, "message" => $data['is_hot'] ? "Product marked as hot" : "Product unmarked as hot"]);

