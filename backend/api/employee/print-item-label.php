<?php
require __DIR__ . '/../../../../vendor/fpdf/fpdf.php';
include __DIR__ . '/../../connect.php';

$order_id = isset($_GET['order_id']) ? intval($_GET['order_id']) : 0;

if ($order_id <= 0) {
    die("Missing or invalid order_id");
}

try {
    // Lấy order items
    $stmt_items = $pdo->prepare("
        SELECT oi.quantity, oi.note, p.product_name
        FROM OrderItems oi
        LEFT JOIN Products p ON oi.product_id = p.id
        WHERE oi.order_id = :order_id
    ");
    $stmt_items->execute([":order_id" => $order_id]);
    $items = $stmt_items->fetchAll(PDO::FETCH_ASSOC);

    if (!$items) die("No items found");

    $pdf = new FPDF('P','mm','A4');
    $pdf->SetAutoPageBreak(false);
    $pdf->AddPage();
    $pdf->SetFont('Arial','B',14);

    $x = 10;
    $y = 10;
    $label_width = 60;
    $label_height = 30;
    $gap = 5;

    foreach ($items as $item) {
        for ($i = 0; $i < $item['quantity']; $i++) {
            // Nếu hết trang, add page mới
            if ($y + $label_height > 297) { // chiều cao A4 = 297mm
                $pdf->AddPage();
                $y = 10;
            }

            // Vẽ khung nhãn
            $pdf->Rect($x, $y, $label_width, $label_height);

            // Nội dung nhãn
            $pdf->SetXY($x+2, $y+2);
            $pdf->MultiCell($label_width-4, 6, $item['product_name'].($item['note'] ? " ({$item['note']})" : ''));

            // Di chuyển y cho nhãn tiếp theo
            $y += $label_height + $gap;
        }
    }

    $pdf->Output();

} catch(Exception $e){
    die("Error generating item labels: ".$e->getMessage());
}
?>
