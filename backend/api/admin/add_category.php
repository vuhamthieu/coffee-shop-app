<?php
include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['category_name'])) {
    echo json_encode(["error" => "category_name is required"]);
    exit;
}

$sql = "INSERT INTO Categories (category_name) VALUES (:category_name)";
$stmt = $pdo->prepare($sql);
$stmt->execute([':category_name' => $data['category_name']]);

echo json_encode([
    "success" => true,
    "id" => $pdo->lastInsertId()
]);
