<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['product_name']) || empty($data['category_id']) || empty($data['price'])) {
    echo json_encode(["error" => "product_name, category_id, and price are required"]);
    exit;
}

$sql = "INSERT INTO Products (product_name, category_id, price, product_image, is_hot, available)
        VALUES (:product_name, :category_id, :price, :product_image, :is_hot, 1)";

$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':product_name' => $data['product_name'],
    ':category_id' => $data['category_id'],
    ':price' => $data['price'],
    ':product_image' => $data['product_image'] ?? null,
    ':is_hot' => $data['is_hot'] ?? 0
]);

echo json_encode([
    "success" => true,
    "id" => $pdo->lastInsertId()
]);
