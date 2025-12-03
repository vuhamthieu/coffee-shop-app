<?php

include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

// Kiểm tra dữ liệu
if (!isset($data['order_id']) || !isset($data['product_id']) || !isset($data['quantity'])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing 'order_id', 'product_id' or 'quantity'"
    ]);
    exit;
}

$order_id   = intval($data['order_id']);
$product_id = intval($data['product_id']);
$quantity   = intval($data['quantity']); // số lượng muốn set, <0 = trừ, >0 = cộng
$note       = isset($data['note']) ? trim($data['note']) : "";

try {
    // Lấy order item hiện tại
    $stmt = $pdo->prepare("SELECT id, quantity FROM OrderItems 
                           WHERE order_id = :order_id AND product_id = :product_id");
    $stmt->execute([
        ":order_id" => $order_id,
        ":product_id" => $product_id
    ]);
    $item = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$item) {
        echo json_encode(["success" => false, "message" => "Order item not found"]);
        exit;
    }

    $new_quantity = $item['quantity'] + $quantity; // cộng/trừ
    if ($new_quantity <= 0) {
        // Xóa món
        $stmt_delete = $pdo->prepare("DELETE FROM OrderItems WHERE id = :id");
        $stmt_delete->execute([":id" => $item['id']]);
    } else {
        // Cập nhật số lượng và note
        $stmt_update = $pdo->prepare("UPDATE OrderItems SET quantity = :qty, note = :note WHERE id = :id");
        $stmt_update->execute([
            ":qty" => $new_quantity,
            ":note" => $note,
            ":id" => $item['id']
        ]);
    }

    // Cập nhật tổng tiền order
    $stmt_total = $pdo->prepare("SELECT SUM(price * quantity) AS total FROM OrderItems WHERE order_id = :order_id");
    $stmt_total->execute([":order_id" => $order_id]);
    $total = $stmt_total->fetch(PDO::FETCH_ASSOC)['total'];
    if (!$total) $total = 0;

    $stmt_update_order = $pdo->prepare("UPDATE Orders SET total = :total WHERE id = :order_id");
    $stmt_update_order->execute([
        ":total" => $total,
        ":order_id" => $order_id
    ]);

    echo json_encode([
        "success" => true,
        "message" => "Order item updated successfully",
        "total" => $total
    ]);

} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Failed to update order item",
        "error" => $e->getMessage()
    ]);
}
?>
