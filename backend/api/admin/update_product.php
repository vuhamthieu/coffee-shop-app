<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['id'])) {
    echo json_encode(["error" => "id is required"]);
    exit;
}

$updates = [];
$params = [':id' => $data['id']];

$allowed = ['product_name', 'category_id', 'price', 'product_image', 'is_hot', 'available'];

foreach ($allowed as $field) {
    if (array_key_exists($field, $data)) {
        $updates[] = "$field = :$field";
        $params[":$field"] = $data[$field];
    }
}

if (empty($updates)) {
    echo json_encode(["error" => "No fields to update"]);
    exit;
}

$sql = "UPDATE Products SET " . implode(", ", $updates) . " WHERE id = :id";
$stmt = $pdo->prepare($sql);
$stmt->execute($params);

echo json_encode(["success" => true, "message" => "Product updated"]);
