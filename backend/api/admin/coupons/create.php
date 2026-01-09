<?php
include __DIR__ . '/../../../connect.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['code']) || empty($data['coupon_type']) || empty($data['coupon_value'])) {
    echo json_encode(["error" => "code, coupon_type, and coupon_value are required"]);
    exit;
}

$coupon_type = $data['coupon_type']; // 'percent' or 'amount'
$code = strtoupper($data['code']);
$coupon_value = $data['coupon_value'];
$min_order = $data['min_order'] ?? 0;
$coupon_start_date = $data['coupon_start_date'] ?? null;
$coupon_end_date = $data['coupon_end_date'] ?? null;
$usage_limit = $data['usage_limit'] ?? 1;

// Check if code already exists
$checkSql = "SELECT id FROM Coupons WHERE code = :code";
$checkStmt = $pdo->prepare($checkSql);
$checkStmt->execute([':code' => $code]);

if ($checkStmt->fetch()) {
    echo json_encode(["error" => "Coupon code already exists"]);
    exit;
}

$sql = "INSERT INTO Coupons (code, coupon_type, coupon_value, min_order, coupon_start_date, coupon_end_date, usage_limit)
        VALUES (:code, :coupon_type, :coupon_value, :min_order, :coupon_start_date, :coupon_end_date, :usage_limit)";

$stmt = $pdo->prepare($sql);
$stmt->execute([
    ':code' => $code,
    ':coupon_type' => $coupon_type,
    ':coupon_value' => $coupon_value,
    ':min_order' => $min_order,
    ':coupon_start_date' => $coupon_start_date,
    ':coupon_end_date' => $coupon_end_date,
    ':usage_limit' => $usage_limit
]);

echo json_encode([
    "success" => true,
    "message" => "Coupon created successfully",
    "coupon_code" => $code
]);
