<?php

include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

// Kiểm tra dữ liệu bắt buộc
if (!isset($data['order_id']) || !isset($data['product_id']) || !isset($data['quantity'])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing 'order_id', 'product_id' or 'quantity'"
    ]);
    exit;
}

$order_id   = intval($data['order_id']);
$product_id = intval($data['product_id']);
$quantity   = intval($data['quantity']);
$note       = isset($data['note']) ? trim($data['note']) : "";

try {
    // Lấy giá sản phẩm tại thời điểm hiện tại
    $stmt = $pdo->prepare("SELECT price, available FROM Products WHERE id = :product_id");
    $stmt->execute([":product_id" => $product_id]);
    $product = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$product) {
        echo json_encode(["success" => false, "message" => "Product not found"]);
        exit;
    }

    if ($product['available'] == 0) {
        echo json_encode(["success" => false, "message" => "Product is not available"]);
        exit;
    }

    $price = $product['price'];

    // Thêm vào OrderItems
    $stmt = $pdo->prepare("INSERT INTO OrderItems (order_id, product_id, quantity, price, note)
                           VALUES (:order_id, :product_id, :quantity, :price, :note)");
    $stmt->execute([
        ":order_id" => $order_id,
        ":product_id" => $product_id,
        ":quantity" => $quantity,
        ":price" => $price,
        ":note" => $note
    ]);

    // Cập nhật tổng tiền trong Orders
    $stmt_total = $pdo->prepare("SELECT SUM(price * quantity) AS total FROM OrderItems WHERE order_id = :order_id");
    $stmt_total->execute([":order_id" => $order_id]);
    $total = $stmt_total->fetch(PDO::FETCH_ASSOC)['total'];

    $stmt_update = $pdo->prepare("UPDATE Orders SET total = :total WHERE id = :order_id");
    $stmt_update->execute([
        ":total" => $total,
        ":order_id" => $order_id
    ]);

    echo json_encode([
        "success" => true,
        "message" => "Order item added successfully",
        "total" => $total
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to add order item",
        "error" => $e->getMessage()
    ]);
}
?>
