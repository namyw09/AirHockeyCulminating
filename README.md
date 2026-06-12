# AirHockeyCulminating

Java Swing air hockey game for the ICS3U culminating project.

## Build And Run

```sh
mkdir -p bin
javac -d bin $(find src -name '*.java')
java -cp bin AirHockeyApp
```

Import this repository root in Eclipse as an existing Java project.

## Sources And Credits

The Candy Battle minigame uses a custom YOLO object-detection model that we
trained on candy photos. We learned the training process from Evan Juras'
Train and Deploy YOLO Models tutorial.

- YOLO training video: https://www.youtube.com/watch?v=r0RspiLG260
- Tutorial repo: https://github.com/EdjeElectronics/Train-and-Deploy-YOLO-Models
- Colab training notebook: https://colab.research.google.com/github/EdjeElectronics/Train-and-Deploy-YOLO-Models/blob/main/Train_YOLO_Models.ipynb
- Label Studio: https://labelstud.io/
- Ultralytics YOLO: https://github.com/ultralytics/ultralytics
- Ultralytics docs: https://docs.ultralytics.com/
- OpenCV: https://opencv.org/
- PyTorch: https://pytorch.org/get-started/locally/
- Anaconda: https://www.anaconda.com/download
- Sound effects: https://sfxr.me/
