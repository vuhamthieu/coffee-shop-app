<?php

include __DIR__ . '/../../connect.php';

try {
    // Lấy tất cả sản phẩm + tên category
    $sql = "
        SELECT 
            p.id,
            p.product_name,
            p.category_id,
            c.category_name,
            p.price,
            p.product_image,
            p.available
        FROM Products p
        JOIN Categories c ON p.category_id = c.id
        ORDER BY p.id DESC
    ";

    $stmt = $pdo->query($sql);
    $products = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "status" => true,
        "message" => "Get products successfully",
        "data" => $products
    ]);

} catch (Exception $e) {
    echo json_encode([
        "status" => false,
        "message" => "Failed to get products",
        "error" => $e->getMessage()
    ]);
}
?>
