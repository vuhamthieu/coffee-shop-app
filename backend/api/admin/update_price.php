<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['id']) || empty($data['price'])) {
    echo json_encode(["error" => "id and price are required"]);
    exit;
}

$sql = "UPDATE Products SET price = :price WHERE id = :id";
$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':price' => $data['price'],
    ':id' => $data['id']
]);

echo json_encode(["success" => true, "message" => "Price updated"]);
