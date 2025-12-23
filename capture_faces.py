import cv2
import os
import numpy as np
import time
import json
from datetime import datetime

class FaceCollector:
    def __init__(self, max_images=150, min_area=30000, max_area=90000, radius=120):
        self.MAX_IMAGES = max_images
        self.MIN_AREA = min_area
        self.MAX_AREA = max_area
        self.RADIUS = radius
        self.DELAY = 0.5
        self.BLUR_THRESHOLD = 100
        
        self.face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")
        self.clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
        self.last_capture = 0
        
    def blur_score(self, img):
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY) if len(img.shape) == 3 else img
        return cv2.Laplacian(gray, cv2.CV_64F).var()
    
    def enhance_image(self, img):
        # Improve lighting and contrast
        lab = cv2.cvtColor(img, cv2.COLOR_BGR2LAB)
        l, a, b = cv2.split(lab)
        l = self.clahe.apply(l)
        enhanced = cv2.cvtColor(cv2.merge([l, a, b]), cv2.COLOR_LAB2BGR)
        
        # Add brightness boost and gamma correction
        enhanced = cv2.convertScaleAbs(enhanced, alpha=1.1, beta=10)  # Slightly brighter
        gamma = 1.2  # Gamma correction for better contrast
        enhanced = np.power(enhanced / 255.0, 1.0 / gamma) * 255
        enhanced = enhanced.astype(np.uint8)
        
        return enhanced
    
    def quality_score(self, face_img):
        blur = min(self.blur_score(face_img) / self.BLUR_THRESHOLD, 1.0)
        gray = cv2.cvtColor(face_img, cv2.COLOR_BGR2GRAY)
        brightness = 1.0 - abs(np.mean(gray) - 127) / 127
        contrast = np.std(gray) / 128
        return (blur + brightness + contrast) / 3
    
    def draw_ui(self, frame, msg, count, quality=None):
        h, w = frame.shape[:2]
        center = (w//2, h//2)
        
        # Guide circle and crosshair
        cv2.circle(frame, center, self.RADIUS, (0,255,0), 3)
        cv2.line(frame, (center[0]-20, center[1]), (center[0]+20, center[1]), (0,255,0), 2)
        cv2.line(frame, (center[0], center[1]-20), (center[0], center[1]+20), (0,255,0), 2)
        
        # Progress bar
        progress = count / self.MAX_IMAGES
        cv2.rectangle(frame, (50, h-80), (w-50, h-60), (100,100,100), -1)
        cv2.rectangle(frame, (50, h-80), (50+int((w-100)*progress), h-60), (0,255,0), -1)
        
        # Text
        cv2.putText(frame, msg, (30, 50), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0,0,255), 2)
        cv2.putText(frame, f"{count}/{self.MAX_IMAGES}", (30, h-90), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255,255,255), 2)
        
        if quality:
            color = (0,255,0) if quality > 0.7 else (0,255,255) if quality > 0.5 else (0,0,255)
            cv2.putText(frame, f"Quality: {quality:.2f}", (30, 90), cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)
    
    def collect(self, name):
        # Create directories
        dirs = {
            'raw': f"dataset/{name}/raw",
            'processed': f"dataset/{name}/processed"
        }
        for d in dirs.values():
            os.makedirs(d, exist_ok=True)
        
        # Setup camera with better settings
        cap = cv2.VideoCapture(0)
        cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
        cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)
        cap.set(cv2.CAP_PROP_BRIGHTNESS, 0.6)     # Increase brightness
        cap.set(cv2.CAP_PROP_CONTRAST, 0.7)       # Increase contrast
        cap.set(cv2.CAP_PROP_SATURATION, 0.6)     # Slight saturation boost
        cap.set(cv2.CAP_PROP_AUTO_EXPOSURE, 0.25) # Manual exposure
        
        count = 0
        stats = {'saved': 0, 'rejected': 0}
        
        print(f"📸 Collecting faces for {name}")
        print("Controls: Q-quit, SPACE-pause, R-reset")
        
        paused = False
        while count < self.MAX_IMAGES:
            if not paused:
                ret, frame = cap.read()
                if not ret: break
                
                frame = cv2.flip(frame, 1)
                enhanced = self.enhance_image(frame)
                gray = cv2.cvtColor(enhanced, cv2.COLOR_BGR2GRAY)
                
                h, w = frame.shape[:2]
                center = (w//2, h//2)
                
                faces = self.face_cascade.detectMultiScale(gray, 1.1, 5, minSize=(50,50))
                msg = "📍 Align face in circle"
                quality = None
                
                if len(faces) == 1:
                    x, y, fw, fh = faces[0]
                    face_center = (x + fw//2, y + fh//2)
                    area = fw * fh
                    
                    # Check position
                    dx, dy = face_center[0] - center[0], face_center[1] - center[1]
                    in_circle = dx*dx + dy*dy <= self.RADIUS*self.RADIUS
                    
                    if in_circle:
                        if area < self.MIN_AREA:
                            msg = "📏 Move closer"
                        elif area > self.MAX_AREA:
                            msg = "📏 Move farther"
                        else:
                            face_img = enhanced[y:y+fh, x:x+fw]
                            quality = self.quality_score(face_img)
                            
                            if time.time() - self.last_capture >= self.DELAY and quality > 0.5:
                                count += 1
                                stats['saved'] += 1
                                
                                # Save images
                                cv2.imwrite(f"{dirs['raw']}/{count:04d}.jpg", face_img)
                                cv2.imwrite(f"{dirs['processed']}/{count:04d}.jpg", 
                                          cv2.resize(face_img, (224, 224)))
                                
                                self.last_capture = time.time()
                                msg = f"✅ Captured {count}"
                                print(f"Saved {count} (Q: {quality:.2f})")
                            else:
                                msg = f"📷 Quality: {quality:.2f}" if quality else "⏳ Ready..."
                                if quality and quality <= 0.5:
                                    stats['rejected'] += 1
                    else:
                        msg = "🎯 Center face in circle"
                    
                    # Draw face box
                    color = (0,255,0) if quality and quality > 0.7 else (0,255,255) if quality and quality > 0.5 else (0,0,255)
                    cv2.rectangle(frame, (x,y), (x+fw, y+fh), color, 2)
                
                elif len(faces) > 1:
                    msg = "👥 Only one face"
                
                self.draw_ui(frame, msg, count, quality)
                cv2.imshow("Face Collector", frame)
            
            key = cv2.waitKey(1) & 0xFF
            if key == ord('q'):
                break
            elif key == ord(' '):
                paused = not paused
                print("⏸️ Paused" if paused else "▶️ Resumed")
            elif key == ord('r'):
                count = 0
                stats = {'saved': 0, 'rejected': 0}
                print("🔄 Reset")
        
        cap.release()
        cv2.destroyAllWindows()
        
        # Save metadata
        metadata = {
            'name': name,
            'date': datetime.now().isoformat(),
            'images': count,
            'stats': stats
        }
        with open(f"dataset/{name}/metadata.json", 'w') as f:
            json.dump(metadata, f, indent=2)
        
        print(f"\n✅ Collected {count} images for {name}")
        print(f"📊 Saved: {stats['saved']}, Rejected: {stats['rejected']}")
        
        return count > 0

# Usage
if __name__ == "__main__":
    name = input("👤 Enter name: ").strip()
    if name:
        collector = FaceCollector()
        collector.collect(name)
    else:
        print("❌ Name required")