import os
import cv2
import pickle
import face_recognition
import argparse
from datetime import datetime
import requests
import numpy as np
import warnings

# Tắt warning về pkg_resources
warnings.filterwarnings('ignore', category=DeprecationWarning)
warnings.filterwarnings('ignore', message='.*pkg_resources.*')

DATASET_DIR = 'dataset'
ENCODING_DIR = 'encodings'
VALID_EXTENSIONS = ('.jpg', '.jpeg', '.png')
MIN_IMAGES_REQUIRED = 10  # số ảnh tối thiểu để encode

# --- HELPER FUNCTIONS ---
def get_people_list(dataset_dir):
    return [name for name in os.listdir(dataset_dir)
            if os.path.isdir(os.path.join(dataset_dir, name))]

def is_valid_image(filename):
    return filename.lower().endswith(VALID_EXTENSIONS)

def preprocess_image(image):
    """
    Xử lý ảnh để đảm bảo đúng định dạng RGB 8-bit
    """
    # Kiểm tra ảnh None
    if image is None:
        return None
    
    # Nếu ảnh có alpha channel (RGBA), chuyển sang RGB
    if image.ndim == 3 and image.shape[2] == 4:
        image = cv2.cvtColor(image, cv2.COLOR_BGRA2BGR)
    
    # Nếu ảnh grayscale, chuyển sang BGR
    elif image.ndim == 2:
        image = cv2.cvtColor(image, cv2.COLOR_GRAY2BGR)
    
    # Kiểm tra image có đúng 3 channels không
    if image.ndim != 3 or image.shape[2] != 3:
        return None
    
    # Đảm bảo dtype là uint8
    if image.dtype != np.uint8:
        if image.max() <= 1.0:  # Nếu ảnh được normalize về [0,1]
            image = (image * 255).astype(np.uint8)
        else:
            image = image.astype(np.uint8)
    
    # Kiểm tra kích thước ảnh (tối thiểu 50x50)
    if image.shape[0] < 50 or image.shape[1] < 50:
        return None
    
    # Resize nếu ảnh quá lớn (giảm thời gian xử lý)
    max_dimension = 1600
    height, width = image.shape[:2]
    if height > max_dimension or width > max_dimension:
        scale = max_dimension / max(height, width)
        new_width = int(width * scale)
        new_height = int(height * scale)
        image = cv2.resize(image, (new_width, new_height), interpolation=cv2.INTER_AREA)
    
    return image

# --- ENCODE ---
def encode_person(person_name, model='hog', force=False):
    processed_path = os.path.join(DATASET_DIR, person_name, 'processed')
    if not os.path.exists(processed_path):
        print(f"Missing folder: {processed_path}")
        return

    image_files = sorted([f for f in os.listdir(processed_path) if is_valid_image(f)])
    if len(image_files) < MIN_IMAGES_REQUIRED:
        print(f"Skipped '{person_name}': not enough images ({len(image_files)} < {MIN_IMAGES_REQUIRED})")
        return

    out_path = os.path.join(ENCODING_DIR, f"{person_name}.pkl")
    if os.path.exists(out_path) and not force:
        print(f"Skipped '{person_name}': already encoded.")
        return

    encodings = []
    failed_images = []
    
    print(f"\n{'='*60}")
    print(f"Processing person: {person_name}")
    print(f"Total images: {len(image_files)}")
    print(f"{'='*60}")
    
    for idx, img_name in enumerate(image_files, 1):
        img_path = os.path.join(processed_path, img_name)
        
        try:
            # Đọc ảnh
            image = cv2.imread(img_path)
            
            if image is None:
                print(f"[{idx}/{len(image_files)}] ❌ Cannot read: {img_name}")
                failed_images.append((img_name, "Cannot read file"))
                continue
            
            # Tiền xử lý ảnh
            image = preprocess_image(image)
            
            if image is None:
                print(f"[{idx}/{len(image_files)}] ❌ Invalid format: {img_name}")
                failed_images.append((img_name, "Invalid image format"))
                continue
            
            # Convert BGR -> RGB
            rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            
            # Kiểm tra lần cuối
            if rgb.dtype != np.uint8:
                print(f"[{idx}/{len(image_files)}] ❌ Wrong dtype: {img_name} (dtype: {rgb.dtype})")
                failed_images.append((img_name, f"Wrong dtype: {rgb.dtype}"))
                continue
            
            if rgb.ndim != 3 or rgb.shape[2] != 3:
                print(f"[{idx}/{len(image_files)}] ❌ Wrong shape: {img_name} (shape: {rgb.shape})")
                failed_images.append((img_name, f"Wrong shape: {rgb.shape}"))
                continue
            
            # Detect faces
            boxes = face_recognition.face_locations(rgb, model=model)
            face_encs = face_recognition.face_encodings(rgb, boxes)
            
            if len(face_encs) == 1:
                encodings.append(face_encs[0])
                print(f"[{idx}/{len(image_files)}] ✓ Encoded: {img_name}")
            elif len(face_encs) == 0:
                print(f"[{idx}/{len(image_files)}] ⚠ No face found: {img_name}")
                failed_images.append((img_name, "No face detected"))
            else:
                print(f"[{idx}/{len(image_files)}] ⚠ Multiple faces ({len(face_encs)}): {img_name}")
                failed_images.append((img_name, f"Multiple faces: {len(face_encs)}"))
                
        except Exception as e:
            print(f"[{idx}/{len(image_files)}] ❌ Error processing {img_name}: {str(e)}")
            failed_images.append((img_name, str(e)))
            continue

    # Tổng kết
    print(f"\n{'='*60}")
    print(f"Summary for '{person_name}':")
    print(f"  - Total images: {len(image_files)}")
    print(f"  - Successfully encoded: {len(encodings)}")
    print(f"  - Failed: {len(failed_images)}")
    print(f"{'='*60}")
    
    if failed_images:
        print("\nFailed images:")
        for img_name, reason in failed_images:
            print(f"  - {img_name}: {reason}")
    
    if encodings:
        os.makedirs(ENCODING_DIR, exist_ok=True)
        with open(out_path, 'wb') as f:
            pickle.dump({'encodings': encodings, 'name': person_name}, f)
        print(f"\n✓ Saved {len(encodings)} encodings to {out_path}")

        # Send embedding to API
        send_embedding_to_api(person_name, encodings)
    else:
        print(f"\n❌ No valid encodings for '{person_name}', nothing saved.")

# --- SEND EMBEDDING TO API ---
def send_embedding_to_api(employee_id, encodings):
    url = "http://localhost:8080/coffee-shop-app/backend/api/admin/employees/add-faceid.php"
    payload = {
        "employee_id": employee_id,
        "embedding": [e.tolist() for e in encodings]
    }
    try:
        print(f"\nSending {len(encodings)} embeddings to API...")
        response = requests.post(url, json=payload, timeout=30)
        
        if response.status_code == 200:
            print(f"✓ API response: {response.text}")
        else:
            print(f"❌ API error (status {response.status_code}): {response.text}")
            
    except requests.exceptions.Timeout:
        print(f"❌ API request timeout")
    except requests.exceptions.ConnectionError:
        print(f"❌ Cannot connect to API server")
    except Exception as e:
        print(f"❌ Failed to send embedding to API: {e}")

# --- ENCODE ALL ---
def encode_faces(model='hog', force=False):
    people = get_people_list(DATASET_DIR)
    print(f"\n{'#'*60}")
    print(f"# Found {len(people)} person(s): {people}")
    print(f"# Model: {model}")
    print(f"# Force re-encode: {force}")
    print(f"{'#'*60}\n")

    for person in people:
        encode_person(person, model=model, force=force)

# --- MAIN ---
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Encode face images into .pkl files and send to API.")
    parser.add_argument('--model', default='hog', choices=['hog', 'cnn'],
                        help='Face detection model to use (default: hog)')
    parser.add_argument('--force', action='store_true',
                        help='Force re-encoding even if .pkl already exists')
    args = parser.parse_args()

    encode_faces(model=args.model, force=args.force)