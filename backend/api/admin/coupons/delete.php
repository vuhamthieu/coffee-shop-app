<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['coupon_id'])) {
    echo json_encode(["error" => "coupon_id is required"]);
    exit;
}

$pdo->beginTransaction();

try {
    // Delete from OrderCoupons first
    $deleteLinkSql = "DELETE FROM OrderCoupons WHERE coupon_id = :coupon_id";
    $deleteLinkStmt = $pdo->prepare($deleteLinkSql);
    $deleteLinkStmt->execute([':coupon_id' => $data['coupon_id']]);

    // Then delete coupon
    $deleteSql = "DELETE FROM Coupons WHERE id = :id";
    $deleteStmt = $pdo->prepare($deleteSql);
    $deleteStmt->execute([':id' => $data['coupon_id']]);

    $pdo->commit();

    echo json_encode([
        "success" => true,
        "message" => "Coupon deleted successfully"
    ]);
} catch (Exception $e) {
    $pdo->rollBack();
    echo json_encode(["error" => "Failed to delete coupon: " . $e->getMessage()]);
}
