import cv2
import face_recognition
import numpy as np
import os
import csv
from datetime import datetime, timedelta
#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import io

# Fix encoding for Windows
if sys.platform == "win32":
    # Cách 1: Set UTF-8 output
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='ignore')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='ignore')
    

# ====================================================================
# A. MÔ PHỎNG TẢI DỮ LIỆU ENCODING
# ====================================================================
from utils.encoding_loader import load_all_encodings


# ====================================================================
# B. HẰNG SỐ CẤU HÌNH HỆ THỐNG VÀ CA LÀM VIỆC
# ====================================================================
LOG_FILE = 'logs/attendance.csv'
RECOGNITION_MODEL = 'hog'
LOG_INTERVAL_MINUTES = 5

EARLY_CHECK_IN_BUFFER = 5 
CHECK_OUT_BUFFER_MINUTES = 5 

SHIFTS = [
    ("Ca 1", 7, 0, 12, 0),
    ("Ca 2", 13, 0, 18, 0),
    ("Ca 3", 18, 0, 23, 0)    # Ca 3 kết thúc 23:35
]

last_logged_times = {}
last_logged_statuses = {} 


# ====================================================================
# C. LOGIC XỬ LÝ TRẠNG THÁI VÀ THỜI GIAN (Không thay đổi)
# ====================================================================
def ensure_log_file():
    os.makedirs(os.path.dirname(LOG_FILE), exist_ok=True)
    if not os.path.exists(LOG_FILE):
        with open(LOG_FILE, 'w', newline='') as f:
            csv.writer(f).writerow(['Name', 'Date', 'Time', 'Status', 'Shift'])

def determine_current_shift():
    now = datetime.now()
    for shift_name, start_hour, start_minute, end_hour, end_minute in SHIFTS:
        shift_start_time = now.replace(hour=start_hour, minute=start_minute, second=0, microsecond=0)
        buffer_start_time = shift_start_time - timedelta(minutes=EARLY_CHECK_IN_BUFFER)
        shift_end_time = now.replace(hour=end_hour, minute=end_minute, second=0, microsecond=0)
        if buffer_start_time > now and shift_start_time.hour < 12: 
            buffer_start_time = buffer_start_time - timedelta(days=1)
        if buffer_start_time <= now < shift_end_time:
            return shift_name
    return "No Shift" 

def is_in_checkout_window(current_shift):
    if current_shift == "No Shift":
        return False
    now = datetime.now()
    for shift_name, start_hour, start_minute, end_hour, end_minute in SHIFTS:
        if shift_name == current_shift:
            shift_end_time = now.replace(hour=end_hour, minute=end_minute, second=0, microsecond=0)
            checkout_start_time = shift_end_time - timedelta(minutes=CHECK_OUT_BUFFER_MINUTES)
            if start_hour > end_hour and now.hour < start_hour:
                 shift_end_time = shift_end_time + timedelta(days=1)
                 checkout_start_time = checkout_start_time + timedelta(days=1)
            if checkout_start_time <= now < shift_end_time:
                return True
    return False

def load_initial_statuses():
    global last_logged_statuses, last_logged_times
    if not os.path.exists(LOG_FILE): return
    with open(LOG_FILE, 'r', newline='') as f:
        reader = csv.reader(f)
        header = next(reader, None) 
        rows = list(reader)
        for row in rows:
            if len(row) >= 5: 
                name, date_str, time_str, status, shift = row[0], row[1], row[2], row[3], row[4]
                last_logged_statuses[name] = (status, shift) 
                try:
                    log_time = datetime.strptime(f"{date_str} {time_str}", "%Y-%m-%d %H:%M:%S")
                    last_logged_times[name] = log_time
                except ValueError: continue

def get_next_status(name, current_shift):
    if current_shift == "No Shift": return None, "No Shift" 
    last_status, last_shift = last_logged_statuses.get(name, ('', ''))
    if last_shift != current_shift:
        return 'Check In', current_shift
    else:
        if last_status == 'Check In':
            return 'Check Out', current_shift
        else:
            return 'Check In', current_shift 

def should_log(name):
    now = datetime.now()
    last_time = last_logged_times.get(name)
    if last_time is None or now - last_time >= timedelta(minutes=LOG_INTERVAL_MINUTES):
        return True
    return False

def log_attendance(name, status, shift):
    global last_logged_times, last_logged_statuses
    now = datetime.now()
    with open(LOG_FILE, 'a', newline='') as f:
        csv.writer(f).writerow([name, now.strftime("%Y-%m-%d"), now.strftime("%H:%M:%S"), status, shift])
    last_logged_statuses[name] = (status, shift)
    last_logged_times[name] = now
    print(f"📝 {name} đã {status.upper()} cho {shift} lúc {now.strftime('%H:%M:%S')}")


# ====================================================================
# D. XỬ LÝ KHUNG HÌNH VÀ HIỂN THỊ
# ====================================================================

def draw_box(frame, box, name, status_shift_text):
    top, right, bottom, left = [v * 4 for v in box]
    color = (0, 255, 0)
    if name == "Unknown" or status_shift_text.startswith("CHỜ:") or status_shift_text.startswith("CHẶN:"):
        color = (0, 0, 255)
    cv2.rectangle(frame, (left, top), (right, bottom), color, 2)
    cv2.putText(frame, name, (left, top - 25), cv2.FONT_HERSHEY_SIMPLEX, 0.9, color, 2)
    cv2.putText(frame, status_shift_text, (left, top - 5), cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

def process_frame(frame, known_encodings, known_names):
    
    small_frame = cv2.resize(frame, (0, 0), fx=0.25, fy=0.25)
    rgb_small = cv2.cvtColor(small_frame, cv2.COLOR_BGR2RGB)

    boxes = face_recognition.face_locations(rgb_small, model=RECOGNITION_MODEL)
    encodings = face_recognition.face_encodings(rgb_small, boxes)

    for encoding, box in zip(encodings, boxes):
        matches = face_recognition.compare_faces(known_encodings, encoding, tolerance=0.5)
        name = "Unknown"
        status_shift_text = "" 

        if True in matches:
            matched_idxs = [i for i, match in enumerate(matches) if match]
            best_match = min(matched_idxs, key=lambda i: face_recognition.face_distance([known_encodings[i]], encoding))
            name = known_names[best_match]

            current_shift = determine_current_shift()
            next_status, next_shift = get_next_status(name, current_shift)
            
            last_status_tuple = last_logged_statuses.get(name, ('Chưa Log', 'N/A'))
            last_status_display, last_shift_display = last_status_tuple
            
            # 1. Xác định text hiển thị mặc định
            if current_shift == "No Shift":
                 status_shift_text = f"Ngoai ca ({last_status_display})"
            elif last_status_display == 'Chua Log' or last_shift_display != current_shift:
                 status_shift_text = "Sẵn sàng Check In"
            else:
                 status_shift_text = f"{last_status_display} ({last_shift_display})"


            # 2. QUYẾT ĐỊNH GHI LOG
            should_perform_log = should_log(name)
            
            # --- LOGIC ĐÃ SỬA ĐỔI: Xử lý Check Out và Check In ---
            is_checkout_time = is_in_checkout_window(current_shift)

            if next_status == 'Check Out':
                if not is_checkout_time:
                    should_perform_log = False
                    end_time_tuple = [(s[3], s[4]) for s in SHIFTS if s[0] == current_shift][0]
                    status_shift_text = f"CHỜ: Check Out sau {end_time_tuple[0]:02d}:{end_time_tuple[1]:02d} - 5 phút"
            
            elif next_status == 'Check In':
                # >>> BỔ SUNG LOGIC CHẶN CHECK IN CUỐI CA <<<
                if is_checkout_time:
                    should_perform_log = False
                    
                # -----------------------------------------------
            
            if next_status and should_perform_log:
                log_attendance(name, next_status, next_shift)
                status_shift_text = f"{next_status} ({next_shift})"

            # Vẽ hộp với tên và trạng thái/ca
            draw_box(frame, box, name, status_shift_text)
        else:
             current_shift = determine_current_shift()
             status_shift_text = f"Unknown (Ca: {current_shift})"
             draw_box(frame, box, name, status_shift_text) 

# ====================================================================
# E. HÀM MAIN (Khởi động)
# ====================================================================

def main():
    print(" Loading encodings...")
    known_encodings, known_names = load_all_encodings()
    
    if not known_encodings or (isinstance(known_encodings, list) and len(known_encodings) == 0):
        print(" No encodings loaded. Please run encode_faces first.")
        return

    ensure_log_file()
    load_initial_statuses()
    
    print(" Encodings loaded. Initial statuses loaded. Starting camera...")
    print(f" Cấu hình Ca làm việc:")
    for name, sh, sm, eh, em in SHIFTS:
        print(f"   - {name}: {sh:02d}:{sm:02d} - {eh:02d}:{em:02d}")
    print(f"   - Check In sớm được phép: {EARLY_CHECK_IN_BUFFER} phút")
    print(f"   - Check Out giới hạn trong: {CHECK_OUT_BUFFER_MINUTES} phút cuối ca")
    print(f" Hiện tại đang là Ca: {determine_current_shift()}")

    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print(" Cannot access camera. Trying index 1...")
        cap = cv2.VideoCapture(1)
        if not cap.isOpened():
             print(" Cannot access camera at index 0 or 1. Please check camera connection/drivers.")
             return

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        frame = cv2.flip(frame, 1)

        process_frame(frame, known_encodings, known_names)
        cv2.imshow("Face Attendance", frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            print(" Exiting...")
            break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()