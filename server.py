# server.py (sửa)
from fastapi import FastAPI
from pydantic import BaseModel
import face_recognition
import cv2
import pickle
import numpy as np
from datetime import datetime

app = FastAPI()

# ============================
# LOAD DỮ LIỆU NHÂN VIÊN (encodings.pkl)
# ============================
ENC_FILE = "encodings.pkl"

try:
    with open(ENC_FILE, "rb") as f:
        data = pickle.load(f)
        KNOWN_ENCODINGS = data.get("encodings", [])
        KNOWN_IDS = data.get("ids", [])
    print("✔ encodings.pkl loaded:", len(KNOWN_IDS), "IDs")
except Exception as e:
    print("LỖI: Không tìm thấy file encodings.pkl hoặc corrupted!", e)
    KNOWN_ENCODINGS = []
    KNOWN_IDS = []

# Ví dụ mapping (nên dùng key là string nếu IDs là "ID_1")
EMPLOYEES = {
    "ID_1": "Alice",
    "ID_2": "Bob",
    "ID_3": "Charlie"
}

# trạng thái cuối cùng (optional)
LAST_STATE = {}  # { "ID_1": "Check-in" }

# ============================
# FORMAT TRẢ VỀ (id là string để có thể chứa "ID_1")
# ============================
class Attendance(BaseModel):
    id: str
    name: str
    type: str
    time: str

# ============================
# API NHẬN DIỆN KHUÔN MẶT
# ============================
@app.get("/recognize", response_model=Attendance)
def recognize_face():
    # Nếu chưa có encodings thì trả về lỗi ý nghĩa
    if not KNOWN_ENCODINGS or not KNOWN_IDS:
        return Attendance(
            id="0",
            name="No encodings",
            type="Error - no encodings",
            time=datetime.now().strftime("%H:%M:%S")
        )

    # mở camera
    cap = cv2.VideoCapture(0)
    try:
        ret, frame = cap.read()
    finally:
        # luôn release camera
        cap.release()

    if not ret or frame is None:
        return Attendance(
            id="0",
            name="Camera Error",
            type="Error",
            time=datetime.now().strftime("%H:%M:%S")
        )

    # xử lý ảnh
    rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    boxes = face_recognition.face_locations(rgb)
    encodings = face_recognition.face_encodings(rgb, boxes)

    if len(encodings) == 0:
        return Attendance(
            id="0",
            name="No Face Detected",
            type="None",
            time=datetime.now().strftime("%H:%M:%S")
        )

    # so khớp từng face (ở đây lấy face đầu tiên)
    face_enc = encodings[0]

    # đảm bảo known list không rỗng
    if len(KNOWN_ENCODINGS) == 0:
        return Attendance(
            id="0",
            name="No encodings",
            type="Error",
            time=datetime.now().strftime("%H:%M:%S")
        )

    distances = face_recognition.face_distance(KNOWN_ENCODINGS, face_enc)
    best_idx = int(np.argmin(distances))
    best_dist = float(distances[best_idx])

    # threshold (tùy bạn điều chỉnh; 0.45 khá chặt)
    THRESHOLD = 0.45

    if best_dist <= THRESHOLD:
        emp_id = str(KNOWN_IDS[best_idx])         # đảm bảo là string
        emp_name = EMPLOYEES.get(emp_id, emp_id)  # nếu không có tên thì trả ID
        # đơn giản toggle check-in/out
        last = LAST_STATE.get(emp_id, "Check-out")
        action = "Check-in" if last == "Check-out" else "Check-out"
        LAST_STATE[emp_id] = action

        return Attendance(
            id=emp_id,
            name=emp_name,
            type=action,
            time=datetime.now().strftime("%H:%M:%S")
        )

    # không khớp
    return Attendance(
        id="0",
        name="Unknown",
        type="Not recognized",
        time=datetime.now().strftime("%H:%M:%S")
    )
