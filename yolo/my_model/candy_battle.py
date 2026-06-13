# Sources used while building the YOLO side of this project:
#   - Training video:   https://www.youtube.com/watch?v=r0RspiLG260
#   - Tutorial repo:    https://github.com/EdjeElectronics/Train-and-Deploy-YOLO-Models
#   - Companion guide:  https://www.ejtech.io/learn/train-yolo-models
#   - Colab notebook:   https://colab.research.google.com/github/EdjeElectronics/Train-and-Deploy-YOLO-Models/blob/main/Train_YOLO_Models.ipynb
#   - Candy dataset:    https://s3.us-west-1.amazonaws.com/evanjuras.com/resources/candy_data_06JAN25.zip
#   - Label Studio:     https://labelstud.io/
#   - Ultralytics YOLO: https://github.com/ultralytics/ultralytics  (docs: https://docs.ultralytics.com/)
#   - OpenCV:           https://opencv.org/
#   - PyTorch:          https://pytorch.org/get-started/locally/
#   - Anaconda:         https://www.anaconda.com/download

import os
import sys
import argparse
import random
import time

import cv2
from ultralytics import YOLO

# Candy Battle minigame for Air Hockey.
# Splits the USB camera down the middle (left = Player 1, right = Player 2).


CANDIES = ['aero', 'coffee-crisp', 'kitkat', 'smarties']

COUNTDOWN_SECONDS = 5     # how long players have to hold up their candy
CAPTURE_WINDOW = 1.0      # aggregate detections over the final second
REVEAL_SECONDS = 3        # how long the winner screen stays up

# Parse arguments (same style as yolo_detect.py)
parser = argparse.ArgumentParser()
parser.add_argument('--model', required=True, help='Path to YOLO model file')
parser.add_argument('--source', required=True, help='USB camera source, e.g. "usb0"')
parser.add_argument('--resolution', default='1280x720', help='Resolution WxH, e.g. "1280x720"')
parser.add_argument('--thresh', default=0.5, help='Minimum confidence threshold')
parser.add_argument('--result', required=True, help='Path to write the result file')
parser.add_argument('--p1name', default='Player 1', help='Player 1 display name')
parser.add_argument('--p2name', default='Player 2', help='Player 2 display name')
args = parser.parse_args()

model_path = args.model
img_source = args.source
min_thresh = float(args.thresh)
result_path = args.result
p1_name = args.p1name
p2_name = args.p2name
resW, resH = int(args.resolution.split('x')[0]), int(args.resolution.split('x')[1])

split_x = resW // 2  # everything left of this is Player 1, right is Player 2


def write_result(target, p1, p2, winner):
    """post: writes the battle outcome to result_path; ignores write errors"""
    try:
        with open(result_path, 'w') as f:
            f.write('target=' + target + '\n')
            f.write('p1=' + ','.join(sorted(p1)) + '\n')
            f.write('p2=' + ','.join(sorted(p2)) + '\n')
            f.write('winner=' + str(winner) + '\n')
    except Exception:
        pass
    # also print so the parent process can see it in the logs
    print('target=' + target)
    print('p1=' + ','.join(sorted(p1)))
    print('p2=' + ','.join(sorted(p2)))
    print('winner=' + str(winner))


# If the model is missing, report no winner so the game never soft-locks
if not os.path.exists(model_path):
    print('ERROR: model not found at ' + model_path)
    write_result('none', set(), set(), 0)
    sys.exit(0)

# the computer's secret pick
target = random.choice(CANDIES)

# Load model and open camera
model = YOLO(model_path, task='detect')
labels = model.names

usb_idx = int(img_source[3:]) if 'usb' in img_source else 0
cap = cv2.VideoCapture(usb_idx)
cap.set(3, resW)
cap.set(4, resH)

if not cap.isOpened():
    print('ERROR: could not open camera')
    write_result(target, set(), set(), 0)
    sys.exit(0)

window = 'Candy Battle'
cv2.namedWindow(window, cv2.WINDOW_NORMAL)

# candy held by each side, gathered during the final capture window
p1_candies = set()
p2_candies = set()
quit_early = False

start_time = time.time()

while True:
    ret, frame = cap.read()
    if (not ret) or (frame is None):
        print('ERROR: lost camera feed')
        break

    frame = cv2.resize(frame, (resW, resH))
    # mirror the image so it reads like a selfie: a player standing on the
    # left appears on the left half (Player 1), matching where they stand
    frame = cv2.flip(frame, 1)

    elapsed = time.time() - start_time
    remaining = COUNTDOWN_SECONDS - elapsed
    if remaining < 0:
        remaining = 0

    capturing = remaining <= CAPTURE_WINDOW  # last second locks in choices

    # Run detection
    results = model(frame, verbose=False)
    detections = results[0].boxes

    for i in range(len(detections)):
        conf = detections[i].conf.item()
        if conf <= min_thresh:
            continue

        xyxy = detections[i].xyxy.cpu().numpy().squeeze().astype(int)
        xmin, ymin, xmax, ymax = xyxy
        classidx = int(detections[i].cls.item())
        classname = labels[classidx]
        center_x = (xmin + xmax) // 2

        # assign to a player based on which half of the frame it is in
        if center_x < split_x:
            color = (230, 124, 54)   # blue-ish for Player 1
            if capturing:
                p1_candies.add(classname)
        else:
            color = (60, 70, 220)    # red-ish for Player 2
            if capturing:
                p2_candies.add(classname)

        cv2.rectangle(frame, (xmin, ymin), (xmax, ymax), color, 2)
        label = classname + ': ' + str(int(conf * 100)) + '%'
        cv2.putText(frame, label, (xmin, max(ymin - 8, 20)),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

    # draw the split line and player labels
    cv2.line(frame, (split_x, 0), (split_x, resH), (255, 255, 255), 2)
    cv2.putText(frame, p1_name, (40, 50),
                cv2.FONT_HERSHEY_SIMPLEX, 1.1, (230, 124, 54), 3)
    cv2.putText(frame, p2_name, (split_x + 40, 50),
                cv2.FONT_HERSHEY_SIMPLEX, 1.1, (60, 70, 220), 3)

    # target stays hidden until the reveal
    cv2.putText(frame, "Computer's pick: ???", (resW // 2 - 180, resH - 30),
                cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 255, 255), 2)

    # big countdown number in the middle-top
    count_text = str(int(remaining) + 1) if remaining > 0 else 'GO'
    cv2.putText(frame, count_text, (resW // 2 - 30, 130),
                cv2.FONT_HERSHEY_SIMPLEX, 3.0, (0, 255, 255), 6)

    cv2.imshow(window, frame)

    key = cv2.waitKey(5)
    if key == ord('q') or key == ord('Q') or key == 27:  # q or ESC
        quit_early = True
        break

    if remaining <= 0:
        break

# decide the winner
if quit_early:
    winner = 0
else:
    target_in_p1 = target in p1_candies
    target_in_p2 = target in p2_candies
    if target_in_p1 and not target_in_p2:
        winner = 1
    elif target_in_p2 and not target_in_p1:
        winner = 2
    else:
        winner = 0  # nobody held it, or (unexpectedly) both did

# reveal screen
reveal_start = time.time()
while not quit_early and time.time() - reveal_start < REVEAL_SECONDS:
    ret, frame = cap.read()
    if (not ret) or (frame is None):
        break
    frame = cv2.resize(frame, (resW, resH))
    frame = cv2.flip(frame, 1)  # keep the same mirrored view on the reveal screen

    cv2.putText(frame, 'Computer picked: ' + target.upper(),
                (resW // 2 - 260, resH // 2 - 40),
                cv2.FONT_HERSHEY_SIMPLEX, 1.2, (0, 255, 255), 3)

    if winner == 0:
        outcome = 'NO WINNER'
    elif winner == 1:
        outcome = p1_name + ' WINS!'
    else:
        outcome = p2_name + ' WINS!'
    # center the outcome text so longer names still sit in the middle
    (text_w, _), _ = cv2.getTextSize(outcome, cv2.FONT_HERSHEY_SIMPLEX, 1.5, 4)
    cv2.putText(frame, outcome, (resW // 2 - text_w // 2, resH // 2 + 40),
                cv2.FONT_HERSHEY_SIMPLEX, 1.5, (0, 255, 0), 4)

    cv2.imshow(window, frame)
    if cv2.waitKey(5) in (ord('q'), ord('Q'), 27):
        break

write_result(target, p1_candies, p2_candies, winner)

cap.release()
cv2.destroyAllWindows()
