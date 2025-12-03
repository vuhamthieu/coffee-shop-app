<?php

include __DIR__ . '/../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['order_id']) || !isset($data['payment_method'])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing 'order_id' or 'payment_method'"
    ]);
    exit;
}

$order_id = intval($data['order_id']);
$payment_method = trim($data['payment_method']); // e.g., 'cash', 'bank transfer'

try {
    $pdo->beginTransaction();

    // Cập nhật Orders: mark paid + lưu thời gian thanh toán + phương thức
    $stmt = $pdo->prepare("UPDATE Orders 
                           SET order_status = 'paid', paid_at = NOW()
                           WHERE id = :order_id");
    $stmt->execute([":order_id" => $order_id]);

    // Có thể lưu thêm log action
    $stmt_log = $pdo->prepare("INSERT INTO ActivityLog (employee_id, action_name, detail) 
                               SELECT employee_id, 'Checkout', CONCAT('Paid via ', :payment_method) 
                               FROM Orders WHERE id = :order_id");
    $stmt_log->execute([
        ":payment_method" => $payment_method,
        ":order_id" => $order_id
    ]);

    $pdo->commit();

    echo json_encode([
        "success" => true,
        "message" => "Order checked out successfully"
    ]);

} catch(Exception $e) {
    $pdo->rollBack();
    echo json_encode([
        "success" => false,
        "message" => "Failed to checkout order",
        "error" => $e->getMessage()
    ]);
}
?>
