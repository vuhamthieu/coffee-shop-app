<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['id'])) {
    echo json_encode(["error" => "id is required"]);
    exit;
}

$sql = "DELETE FROM Categories WHERE id = :id";
$stmt = $pdo->prepare($sql);
$stmt->execute([':id' => $data['id']]);

echo json_encode(["success" => true, "message" => "Category deleted"]);
