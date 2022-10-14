import tensorflow
# Helps in parsing images
from tensorflow.keras.preprocessing import image
from tensorflow.keras.layers import GlobalMaxPooling2D
from tensorflow.keras.applications.resnet50 import ResNet50, preprocess_input
import numpy as np
from numpy.linalg import norm
import os
# this model helps in getting the progess of for loop
from tqdm import tqdm
import pickle

model = ResNet50(weights='imagenet', include_top=False, input_shape=(224,224,3))
# input shape is the standard scaled down resolution of the image to be fed into the model

# since we don't want to train the model. We only use this model for prediction.
model.trainable = False

# we remove the top[last] layer of the model and put our layer
model = tensorflow.keras.Sequential([
    model,
    GlobalMaxPooling2D()
])

# print(model.summary())
# Model: "sequential"
# _________________________________________________________________
# Layer (type)                 Output Shape              Param #
# =================================================================
# resnet50 (Functional)        (None, 7, 7, 2048)        23587712
# _________________________________________________________________
# global_max_pooling2d (Global (None, 2048)              0
# =================================================================
# Total params: 23,587,712
# Trainable params: 0
# Non-trainable params: 23,587,712
# _________________________________________________________________


def extract_features(img_path,model):
    img = image.load_img(img_path,target_size=(224,224))

    # converting image to numpy array : RGB values (3). Shape - 224, 224, 3  : 3D Array
    img_array = image.img_to_array(img)

    # reshaping into (1, 224, 224, 3) since we need to pass images as batches in the keras model - 4D array
    expanded_img_array = np.expand_dims(img_array, axis=0)

    # preprocess_input function of resnet convert the input in model required format. The images are converted from rgb to bgr, then each color channel is zero centered with respect to imageNet Dataset, without scaling
    preprocessed_img = preprocess_input(expanded_img_array)

    # Making predictions - (1, 2048). By  using flatten, we are bringing it to 1D array
    result = model.predict(preprocessed_img).flatten()

    # We normalize the value, bring them btw 0 to 1. By diving by the l2norm of the resnet. Norm = sqrt(sum of sq of each value in 1D array)
    normalized_result = result / norm(result)

    return normalized_result

# to store the paths of images
# print(os.listdir('../datasets/archive/images'))
clothes_images_filenames = []

for file in os.listdir('../datasets/clothes_images'):
    clothes_images_filenames.append(os.path.join('../static/img/clothes',file))
# print(filenames)
# print(len(filenames))
# 44441

clothes_feature_list = []
for file in tqdm(clothes_images_filenames):
    clothes_feature_list.append(extract_features(file,model))

pickle.dump(clothes_feature_list,open('../pickle/clothes_feature_list.pkl','wb'))
pickle.dump(clothes_images_filenames,open('../pickle/clothes_images_filenames.pkl','wb'))