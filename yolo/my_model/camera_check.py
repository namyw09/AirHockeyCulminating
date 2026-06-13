# originally used to debug my computer to find which camera model my mac uses (usb0 v.s. usb1)

import cv2
for i in range(5):
    cap = cv2.VideoCapture(i)
    if cap.isOpened():
        print(f'Camera found at index {i}')
        cap.release()

