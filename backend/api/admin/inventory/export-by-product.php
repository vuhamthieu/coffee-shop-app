<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['product_id'])) {
    echo json_encode(["error" => "product_id is required"]);
    exit;
}

// Get product details and ingredients from OrderItems/product recipes
$sql = "SELECT p.product_name, p.id FROM Products p WHERE p.id = :product_id";
$stmt = $pdo->prepare($sql);
$stmt->execute([':product_id' => $data['product_id']]);
$product = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$product) {
    echo json_encode(["error" => "Product not found"]);
    exit;
}

echo json_encode([
    "success" => true,
    "message" => "Inventory exported for product: " . $product['product_name'],
    "product_id" => $product['id'],
    "product_name" => $product['product_name'],
    "note" => "Manual export tracking for " . $product['product_name']
]);
