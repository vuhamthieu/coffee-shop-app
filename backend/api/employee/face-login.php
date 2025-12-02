<?php

    include __DIR__ . '/../../connect.php';

    // Đọc JSON input
    $input = json_decode(file_get_contents("php://input"), true);

    if (!isset($input['embedding'])) {
        echo json_encode([
            "success" => false,
            "message" => "Missing embedding"
        ]);
        exit;
    }

    $inputEmbedding = $input['embedding']; // array float 128/256/512 chiều
    if (!is_array($inputEmbedding)) {
        echo json_encode([
            "success" => false,
            "message" => "Embedding must be array"
        ]);
        exit;
    }

    // Lấy toàn bộ FaceData để so khớp
    $sql = "SELECT f.id, f.employee_id, f.embedding, e.employee_name, e.role_id, r.role_name, e.active
            FROM FaceData f
            JOIN Employees e ON f.employee_id = e.id
            JOIN Roles r ON e.role_id = r.id";

    $result = $conn->query($sql);

    if ($result->num_rows == 0) {
        echo json_encode([
            "success" => false,
            "message" => "No face data registered"
        ]);
        exit;
    }

    // Hàm tính cosine similarity
    function cosine_similarity($a, $b) {
        $dot = 0;
        $normA = 0;
        $normB = 0;

        for ($i = 0; $i < count($a); $i++) {
            $dot += $a[$i] * $b[$i];
            $normA += $a[$i] * $a[$i];
            $normB += $b[$i] * $b[$i];
        }

        $denom = (sqrt($normA) * sqrt($normB));
        return $denom == 0 ? 0 : $dot / $denom;
    }

    $bestMatch = null;
    $bestScore = -1;

    while ($row = $result->fetch_assoc()) {
        // Giải mã embedding từ LONGBLOB → array float
        $storedEmbedding = json_decode($row['embedding'], true);

        if (!is_array($storedEmbedding)) continue;

        // Tính độ giống
        $score = cosine_similarity($inputEmbedding, $storedEmbedding);

        if ($score > $bestScore) {
            $bestScore = $score;
            $bestMatch = $row;
        }
    }

    // Ngưỡng nhận diện (tùy mô hình: 0.75 – 0.85)
    $THRESHOLD = 0.82;

    if ($bestScore < $THRESHOLD || !$bestMatch) {
        echo json_encode([
            "success" => false,
            "message" => "Face not recognized",
            "score"   => $bestScore
        ]);
        exit;
    }

    // Kiểm tra tài khoản active
    if ((int)$bestMatch['active'] === 0) {
        echo json_encode([
            "success" => false,
            "message" => "Account is deactivated"
        ]);
        exit;
    }

    // Ghi log
    $log_stmt = $conn->prepare("
        INSERT INTO ActivityLog (employee_id, action_name, detail)
        VALUES (?, 'face_login', 'Face login success')
    ");
    $log_stmt->bind_param("i", $bestMatch['employee_id']);
    $log_stmt->execute();

    // Trả kết quả
    echo json_encode([
        "success" => true,
        "message" => "Face login success",
        "score" => $bestScore,
        "employee" => [
            "id" => $bestMatch['employee_id'],
            "name" => $bestMatch['employee_name'],
            "role_id" => $bestMatch['role_id'],
            "role_name" => $bestMatch['role_name']
        ]
    ]);
?>
