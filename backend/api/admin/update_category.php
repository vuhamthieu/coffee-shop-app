<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['id']) || empty($data['category_name'])) {
    echo json_encode(["error" => "id and category_name are required"]);
    exit;
}

$sql = "UPDATE Categories SET category_name = :category_name WHERE id = :id";
$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':category_name' => $data['category_name'],
    ':id' => $data['id']
]);

echo json_encode(["success" => true, "message" => "Category updated"]);
