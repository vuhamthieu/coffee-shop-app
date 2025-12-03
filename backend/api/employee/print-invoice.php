<?php
require __DIR__ . '/../../../../vendor/fpdf/fpdf.php'; // thư viện FPDF
include __DIR__ . '/../../connect.php';

$order_id = isset($_GET['order_id']) ? intval($_GET['order_id']) : 0;

if ($order_id <= 0) {
    die("Missing or invalid order_id");
}

try {
    // Lấy thông tin order
    $stmt_order = $pdo->prepare("
        SELECT o.id, o.table_id, o.employee_id, o.total, o.order_status, o.created_at, o.paid_at,
               t.table_name, e.employee_name
        FROM Orders o
        LEFT JOIN Tables t ON o.table_id = t.id
        LEFT JOIN Employees e ON o.employee_id = e.id
        WHERE o.id = :order_id
    ");
    $stmt_order->execute([":order_id" => $order_id]);
    $order = $stmt_order->fetch(PDO::FETCH_ASSOC);

    if (!$order) die("Order not found");

    // Lấy danh sách order items
    $stmt_items = $pdo->prepare("
        SELECT oi.quantity, oi.price, oi.note, p.product_name
        FROM OrderItems oi
        LEFT JOIN Products p ON oi.product_id = p.id
        WHERE oi.order_id = :order_id
    ");
    $stmt_items->execute([":order_id" => $order_id]);
    $items = $stmt_items->fetchAll(PDO::FETCH_ASSOC);

    // Lấy coupon
    $stmt_coupon = $pdo->prepare("
        SELECT c.code, c.coupon_type, c.coupon_value, oc.discount
        FROM OrderCoupons oc
        LEFT JOIN Coupons c ON oc.coupon_id = c.id
        WHERE oc.order_id = :order_id
    ");
    $stmt_coupon->execute([":order_id" => $order_id]);
    $coupons = $stmt_coupon->fetchAll(PDO::FETCH_ASSOC);

    // Tạo PDF
    $pdf = new FPDF();
    $pdf->AddPage();
    $pdf->SetFont('Arial','B',16);
    $pdf->Cell(0,10,'HOA DON QUAN CAFE',0,1,'C');

    $pdf->SetFont('Arial','',12);
    $pdf->Cell(0,10,'Order ID: '.$order['id'],0,1);
    $pdf->Cell(0,10,'Table: '.$order['table_name'],0,1);
    $pdf->Cell(0,10,'Employee: '.$order['employee_name'],0,1);
    $pdf->Cell(0,10,'Created at: '.$order['created_at'],0,1);
    if($order['paid_at']) $pdf->Cell(0,10,'Paid at: '.$order['paid_at'],0,1);

    $pdf->Ln(5);
    $pdf->SetFont('Arial','B',12);
    $pdf->Cell(80,10,'Product',1);
    $pdf->Cell(20,10,'Qty',1);
    $pdf->Cell(30,10,'Price',1);
    $pdf->Cell(30,10,'Total',1);
    $pdf->Ln();

    $pdf->SetFont('Arial','',12);
    $subtotal = 0;
    foreach($items as $item){
        $line_total = $item['price'] * $item['quantity'];
        $subtotal += $line_total;

        $pdf->Cell(80,10,$item['product_name'].($item['note'] ? ' ('.$item['note'].')' : ''),1);
        $pdf->Cell(20,10,$item['quantity'],1);
        $pdf->Cell(30,10,number_format($item['price']),1);
        $pdf->Cell(30,10,number_format($line_total),1);
        $pdf->Ln();
    }

    $pdf->Ln(3);
    $pdf->Cell(130,10,'Subtotal',0,0,'R');
    $pdf->Cell(30,10,number_format($subtotal),1);
    $pdf->Ln();

    $total_discount = 0;
    if($coupons){
        foreach($coupons as $c){
            $pdf->Cell(130,10,'Coupon '.$c['code'],0,0,'R');
            $pdf->Cell(30,10,'-'.number_format($c['discount']),1);
            $pdf->Ln();
            $total_discount += $c['discount'];
        }
    }

    $total = max($subtotal - $total_discount,0);
    $pdf->SetFont('Arial','B',12);
    $pdf->Cell(130,10,'TOTAL',0,0,'R');
    $pdf->Cell(30,10,number_format($total),1);
    $pdf->Ln(20);

    $pdf->SetFont('Arial','I',10);
    $pdf->Cell(0,10,'Thank you for visiting!',0,1,'C');

    $pdf->Output(); // hiển thị trực tiếp trên trình duyệt

} catch(Exception $e){
    die("Error generating invoice: ".$e->getMessage());
}
?>
