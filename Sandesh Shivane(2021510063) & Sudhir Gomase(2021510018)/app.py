
import numpy as np
import cv2
import streamlit as st
from tensorflow import keras
from keras.models import load_model
from tensorflow.keras.utils import img_to_array
from streamlit_webrtc import webrtc_streamer, VideoTransformerBase

# load model

emotion_dict = ['Angry','Happy','Neutral','Sad','Surprise'] 



classifier =load_model('pretraining.h5')

classifier.load_weights("pretraining.h5")

#load face
try:
    face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')
except Exception:
    st.write("Error loading cascade classifiers")

class VideoTransformer(VideoTransformerBase):
    def transform(self, frame):
        img = frame.to_ndarray(format="bgr24")

        #image gray
        img_gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        faces = face_cascade.detectMultiScale(
            image=img_gray, scaleFactor=1.3, minNeighbors=5)
        for (x, y, w, h) in faces:
            cv2.rectangle(img=img, pt1=(x, y), pt2=(
                x + w, y + h), color=(255, 0, 0), thickness=2)
            roi_gray = img_gray[y:y + h, x:x + w]
            roi_gray = cv2.resize(roi_gray, (48, 48), interpolation=cv2.INTER_AREA)
            if np.sum([roi_gray]) != 0:
                roi = roi_gray.astype('float') / 255.0
                roi = img_to_array(roi)
                roi = np.expand_dims(roi, axis=0)
                prediction = classifier.predict(roi)[0]
                maxindex = int(np.argmax(prediction))
                finalout = emotion_dict[maxindex]
                output = str(finalout)
            label_position = (x, y)
            cv2.putText(img, output, label_position, cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

        return img

def main():
    # Face Analysis Application #
    st.title("Real Time Face Emotion Detection Application")
    activiteis = [ "Home", "Project Info", "Webcam Face Detection", "About Us"]
    choice = st.sidebar.selectbox("SELECT ACTIVITY", activiteis)
    info=""" <div><h2 style="text-align:center; color:#FF6347;">DEVELOPED BY:</h2> 
            <b>SUDHIR GOMASE </b><br>
            UCID : 2021510018 <br><br>
            <b>SANDESH SHIVANE </b><br>
            UCID : 2021510063 </div>"""
    st.sidebar.markdown(info, unsafe_allow_html=True)

    contact=""" <div><h2 style="text-align:center; color:#FF6347;">CONTACT DETAILS:</h2> 
            <b>SUDHIR GOMASE </b><br>
            Email  : sudhirgomase2109@gmail.com <br>
            Mob No.: 8108320614<br><br>
            <b>SANDESH SHIVANE </b><br>
            Email  : sandeshshivane001@gmail.com <br>
            Mob No.: 8104985407</div>"""
    st.sidebar.markdown(contact, unsafe_allow_html=True)

    if choice == "Home":
        html_temp_home1 = """<div style="background-color:#6D7B8D;padding:10px">
                                            <h4 style="color:white;text-align:center;">
                                            Face Emotion detection application using OpenCV, Custom CNN model and Streamlit.</h4>
                                            </div>
                                            </br>"""
        st.markdown(html_temp_home1, unsafe_allow_html=True)
        st.write("""
                 The application has two functionalities.

                 1. Real time face detection using web cam feed.

                 2. Real time face emotion recognization.

                 """)
    elif choice == "Webcam Face Detection":
        st.header("Webcam Live Feed")
        st.write("Click on start to use webcam and detect your face emotion")
        webrtc_streamer(key="example", video_transformer_factory=VideoTransformer)


    elif choice == "Project Info":
        st.subheader("About this Project Application")
        

        # from PIL import Image
        # image = Image.open('lisa.jpg')

        # st.image(image, caption='Sunrise by the mountains', width=400)

        pro= """<div style="background-color:#6D7B8D;padding:10px">
                                    <h4 style="color:white;text-align:center;">
                                    Real time face emotion detection application using OpenCV, Trained CNN model and Streamlit.</h4>
                                    </div>
                                    </br>"""
        st.markdown(pro, unsafe_allow_html=True)

        proinfo1 = """
            <div style="background-color:#6D7B8D;padding:10px">
            <h4 style="color:white;text-align:center;padding:10px"">Human emotion detection is implemented in many areas requiring additional security or 
            information about the person. It can be seen as a second step to face detection where we may be 
            required to set up a second layer of security, where along with the face, the emotion is also 
            detected. </div>
            <br>"""

        st.markdown(proinfo1, unsafe_allow_html=True)

        proinfo2 = """
            <div style="background-color:#6D7B8D;padding:10px">
            <h4 style="color:white;text-align:center;padding:10px"">The objective of this project is to develop Automatic Facial Expression Recognition System 
            which can take human facial images containing some expression as input and recognize and 
            classify it into five different expression class such as : 1. Happy 2. Sad 3. Neutral 4. Angry 5. surprise
             </div>
            <br>"""

        st.markdown(proinfo2, unsafe_allow_html=True)


    elif choice == "About Us":
        
        st.subheader("About Us")


        ab1 = """
            <div style="background-color:#6D7B8D; padding:15px">
            <h4 style="color:white;text-align:center;">This Application is developed by Sandesh Shivane and Sudhir Gomase using Streamlit Framework, Opencv, Tensorflow and Keras library as a Mini Project for Summer Term. Dataset is taken from Kaggle. This Project is implemented under the guidance of Internal Supervisor Prof. Harshil Kanakia.</h4>
            <h4 style="color:white;text-align:center;">Thanks for Visiting...!!</h4>
            </div>
            <br></br>
            <br></br>"""

        st.markdown(ab1, unsafe_allow_html=True)
    
    else:
        pass


if __name__ == "__main__":
    main()