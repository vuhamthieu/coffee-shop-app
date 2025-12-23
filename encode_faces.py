import os
import cv2
import pickle
import face_recognition
import argparse
from datetime import datetime

DATASET_DIR = 'dataset'
ENCODING_DIR = 'encodings'
LOG_FILE = 'logs/encode_errors.txt'
VALID_EXTENSIONS = ('.jpg', '.jpeg', '.png')
MIN_IMAGES_REQUIRED = 10  # số ảnh tối thiểu để encode

def get_people_list(dataset_dir):
    return [name for name in os.listdir(dataset_dir)
            if os.path.isdir(os.path.join(dataset_dir, name))]

def is_valid_image(filename):
    return filename.lower().endswith(VALID_EXTENSIONS)

def log_error(message):
    os.makedirs(os.path.dirname(LOG_FILE), exist_ok=True)
    with open(LOG_FILE, 'a') as f:
        f.write(f"[{datetime.now()}] {message}\n")

def encode_person(person_name, model='hog', force=False):
    processed_path = os.path.join(DATASET_DIR, person_name, 'processed')
    if not os.path.exists(processed_path):
        msg = f"⚠️ Missing folder: {processed_path}"
        print(msg)
        log_error(msg)
        return

    image_files = sorted([
        f for f in os.listdir(processed_path)
        if is_valid_image(f)
    ])

    if len(image_files) < MIN_IMAGES_REQUIRED:
        msg = f"⚠️ Skipped '{person_name}': not enough images ({len(image_files)} < {MIN_IMAGES_REQUIRED})"
        print(msg)
        log_error(msg)
        return

    out_path = os.path.join(ENCODING_DIR, f"{person_name}.pkl")
    if os.path.exists(out_path) and not force:
        print(f"⏩ Skipped '{person_name}': already encoded.")
        return

    encodings = []

    for img_name in image_files:
        img_path = os.path.join(processed_path, img_name)
        image = cv2.imread(img_path)
        if image is None:
            msg = f"⚠️ Cannot read image: {person_name}/{img_name}"
            print(msg)
            log_error(msg)
            continue

        rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        boxes = face_recognition.face_locations(rgb, model=model)
        face_encs = face_recognition.face_encodings(rgb, boxes)

        if len(face_encs) == 1:
            encodings.append(face_encs[0])
            print(f"✅ Encoded: {person_name}/{img_name}")
        else:
            msg = f"❌ Skipped: {person_name}/{img_name} (faces found: {len(face_encs)})"
            print(msg)
            log_error(msg)

    if encodings:
        os.makedirs(ENCODING_DIR, exist_ok=True)
        with open(out_path, 'wb') as f:
            pickle.dump({
                'encodings': encodings,
                'name': person_name
            }, f)
        print(f"💾 Saved {len(encodings)} encodings for '{person_name}' to {out_path}")
    else:
        print(f"⚠️ No valid encodings for '{person_name}', nothing saved.")

def encode_faces(model='hog', force=False):
    people = get_people_list(DATASET_DIR)
    print(f"🧍‍♂️ Found {len(people)} person(s): {people}")

    for person in people:
        encode_person(person, model=model, force=force)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Encode face images into .pkl files.")
    parser.add_argument('--model', default='hog', choices=['hog', 'cnn'],
                        help='Face detection model to use (default: hog)')
    parser.add_argument('--force', action='store_true',
                        help='Force re-encoding even if .pkl already exists')
    args = parser.parse_args()

    encode_faces(model=args.model, force=args.force)