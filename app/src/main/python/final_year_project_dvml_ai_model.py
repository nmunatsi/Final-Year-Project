# -*- coding: utf-8 -*-
"""final year project DVML AI model.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1lAUG9Bemr-fhsbEv-3aGIMO8c7XklLuP
"""

from tensorflow import keras
import librosa
import numpy as np
import os
import joblib


def get_prediction(path):
    # model_path=Path("/storage/emulated/0/AI model/SER_model.h5")
    model= keras.models.load_model("/storage/emulated/0/AI model/SER_model.h5")
    lst = []
    for subdir, dirs, files in os.walk(path):
        for file in files:
            try:
                #Load librosa array, obtain mfcss, store the file and the mcss information in a new array
                (X, sample_rate) = librosa.load(os.path.join(subdir,file), res_type='kaiser_fast')
                mfccs = np.mean(librosa.feature.mfcc(y=X, sr=sample_rate, n_mfcc=40).T,axis=0)
                # The instruction below converts the labels (from 1 to 8) to a series from 0 to 7
                # This is because our predictor needs to start from 0 otherwise it will try to predict also 0.
                file = int(file[7:8]) - 1
                arr = mfccs, file
                lst.append(arr)
            # If the file is not valid, skip it
            except ValueError:
                continue

    zippedList = zip(*lst)

    print("zippedList",zippedList)

    zippedListAsnumpay = np.asarray(zippedList)

    X_name = 'X.joblib'

    save_dir = '/storage/emulated/0/Ravtess_model'

    savedX = joblib.dump(zippedListAsnumpay, os.path.join(save_dir, X_name))

    testFile = joblib.load('/storage/emulated/0/Ravtess_model/X.joblib')

    x_testcnn = np.expand_dims(zippedListAsnumpay, axis=2)

    print("testInput:", x_testcnn)

    predictions = model.predict_classes(x_testcnn)
    print(predictions)

    return predictions
