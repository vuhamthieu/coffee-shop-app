<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['coupon_id'])) {
    echo json_encode(["error" => "coupon_id is required"]);
    exit;
}

$coupon_id = $data['coupon_id'];

// Get current coupon
$sql = "SELECT * FROM Coupons WHERE id = :id";
$stmt = $pdo->prepare($sql);
$stmt->execute([':id' => $coupon_id]);
$coupon = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$coupon) {
    echo json_encode(["error" => "Coupon not found"]);
    exit;
}

// Update fields if provided
$updateSql = "UPDATE Coupons SET ";
$updateFields = [];
$params = [':id' => $coupon_id];

if (isset($data['coupon_value'])) {
    $updateFields[] = "coupon_value = :coupon_value";
    $params[':coupon_value'] = $data['coupon_value'];
}

if (isset($data['min_order'])) {
    $updateFields[] = "min_order = :min_order";
    $params[':min_order'] = $data['min_order'];
}

if (isset($data['coupon_start_date'])) {
    $updateFields[] = "coupon_start_date = :coupon_start_date";
    $params[':coupon_start_date'] = $data['coupon_start_date'];
}

if (isset($data['coupon_end_date'])) {
    $updateFields[] = "coupon_end_date = :coupon_end_date";
    $params[':coupon_end_date'] = $data['coupon_end_date'];
}

if (isset($data['usage_limit'])) {
    $updateFields[] = "usage_limit = :usage_limit";
    $params[':usage_limit'] = $data['usage_limit'];
}

if (empty($updateFields)) {
    echo json_encode(["error" => "No fields to update"]);
    exit;
}

$updateSql .= implode(", ", $updateFields) . " WHERE id = :id";

$updateStmt = $pdo->prepare($updateSql);
$updateStmt->execute($params);

echo json_encode([
    "success" => true,
    "message" => "Coupon updated successfully"
]);
