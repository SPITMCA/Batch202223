import math
from cvzone.ClassificationModule import Classifier
import cv2
from cvzone.HandTrackingModule import HandDetector
import numpy as np
from pygame._sdl2 import get_audio_device_names
from pygame import mixer
import time
import pyttsx3
import os


cap = cv2.VideoCapture(0)
detector = HandDetector(maxHands=1)
classifier = Classifier("Model/keras_model.h5", "Model/labels.txt")
offset = 20
imgSize = 300
folder = "Data/C"
counter = 0
# last element in labels kept empty intentionally
labels = ["A", "B", "C", "Okay", "Thumbs_Up", "Thumbs_Down", "L", "Victory", ""]
last_predicted_index = 99999
index = 99999
has_changed = True

#AUDIO CONFIGURATION START

# imported pyttsx3, pygame dev version
# Outputs: ['CABLE Input (VB-Audio Virtual Cable)', 'SAMSUNG (NVIDIA High Definition Audio)', 'Realtek HD Audio 2nd output (Realtek(R) Audio)']

mixer.init(devicename = 'CABLE Input (VB-Audio Virtual Cable)')

engine = pyttsx3.init()
voices = engine.getProperty('voices')
voice_female = (voices[1]).id
engine.setProperty('voice', voice_female)
rate = engine.getProperty('rate')
engine.setProperty('rate', rate-50)
volume = engine.getProperty('volume')
engine.setProperty('volume', volume+1)

phrase = "Please predict first."

def prediction_to_speech(index):
    engine.save_to_file(labels[index], 'output_temp.wav')
    engine.runAndWait()
    mixer.music.load("output_temp.wav")
    mixer.music.play()

#AUDIO CONFIGURATION END

# FUNCTION TO DRAW PREDICTED VALUE ON OUTPUT
def draw_predicted_value(index):
    if index!=4:
        cv2.putText(imgOutput, labels[index], (x, y - 26), cv2.FONT_HERSHEY_COMPLEX, 2, (0, 0, 0), 2)
        cv2.rectangle(imgOutput, (x - offset, y - offset), (x + w + offset, y + h + offset), (255, 0, 255), 4)



while True:
    success, img = cap.read()
    imgOutput = img.copy()
    hands, img = detector.findHands(img)
    try:
        draw_predicted_value(index)
    except IndexError:
        pass

    if hands:
        hand = hands[0]
        x, y,w,h = hand['bbox']
        imgCrop = img[y-offset:y+h+offset, x-offset:x+w+offset]
        imgWhite = np.ones((imgSize, imgSize,3), np.uint8)*255
        imgCropShape = imgCrop.shape
        aspectRatio = h/w

        if aspectRatio > 1:
            k = imgSize/h
            wCal = math.ceil(k*w)
            imgResize = cv2.resize(imgCrop, (wCal, imgSize))
            imgResizeShape = imgResize.shape
            wGap = math.ceil((imgSize-wCal)/2)
            imgWhite[:, wGap:wCal+wGap] = imgResize
            prediction, index = classifier.getPrediction(imgWhite, draw=False)
            if(index != last_predicted_index):
                draw_predicted_value(index)
                try:
                    mixer.music.unload()
                except:
                    pass
                prediction_to_speech(index)
                last_predicted_index = index

        else:
            k = imgSize / w
            hCal = math.ceil(k * h)
            imgResize = cv2.resize(imgCrop, (imgSize, hCal))
            imgResizeShape = imgResize.shape
            hGap = math.ceil((imgSize - hCal) / 2)
            imgWhite[hGap:hCal + hGap, :] = imgResize
            prediction, index = classifier.getPrediction(imgWhite, draw=False)
            if (index != last_predicted_index):
                draw_predicted_value(index)
                try:
                    mixer.music.unload()
                except:
                    pass
                prediction_to_speech(index)
                last_predicted_index = index


        # print(prediction, index)
        # cv2.rectangle(imgOutput, (x - offset, y - offset -50), (x - offset + 90, y - offset), (255, 0, 255), cv2.FILLED)
        # cv2.putText(imgOutput, labels[index], (x, y-26), cv2.FONT_HERSHEY_COMPLEX, 2, (0,0,0), 2)
        # cv2.rectangle(imgOutput, (x -offset,y-offset), (x+w+offset, y+h+offset), (255,0,255), 4)
        # cv2.imshow("ImageCrop", imgCrop)
        # cv2.imshow("ImageWhite", imgWhite)

    else:
        index = 4

    cv2.imshow("Image", imgOutput)
    cv2.waitKey(1)



# QUITING THE MIXER
mixer.quit()

# DELETEING TEMP MP3 FILE
os.remove("output_temp.wav")