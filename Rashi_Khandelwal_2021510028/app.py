import re
import pandas as pd
import numpy as np
import string
import pickle
import nltk
import distance
import os
import requests
import tensorflow
from bs4 import BeautifulSoup
from flask import Flask, render_template, request, jsonify
from nltk.corpus import stopwords
from nltk.stem.porter import PorterStemmer
from sklearn.preprocessing import RobustScaler
from fuzzywuzzy import fuzz
from sklearn.neighbors import NearestNeighbors
from tensorflow.keras.preprocessing import image
from werkzeug.utils import secure_filename
from tensorflow.keras.preprocessing import image
from tensorflow.keras.layers import GlobalMaxPooling2D
from tensorflow.keras.applications.resnet50 import ResNet50, preprocess_input
from numpy.linalg import norm
from tqdm import tqdm

app = Flask(__name__)

# Loading Preprocessed Datasets
house_data=pd.read_csv('datasets/Cleaned_Bengaluru_House_Data.csv')
laptop_data=pickle.load(open('pickle/LaptopPrice_ProcessedData.pkl', 'rb'))
clothes_feature_list=pickle.load(open('pickle/clothes_feature_list.pkl','rb'))
clothes_images_filenames=pickle.load(open('pickle/clothes_images_filenames.pkl','rb'))

# Loading pre-trained models
diabetes_model = pickle.load(open('pickle/DiabetesPrediction_RandomForest.pkl', 'rb'))
house_model = pickle.load(open('pickle/BengaluruHousePrice_RidgeModel.pkl', 'rb'))
laptopprice_model=pickle.load(open('pickle/LaptopPrice_RandomForest.pkl', 'rb'))
emailspamham_tfidf = pickle.load(open('pickle/EmailSpamHam_tfidf_vectorizer.pkl','rb'))
emailspamham_model = pickle.load(open('pickle/EmailSpamHam_MultinominalNaiveBayesModel.pkl','rb'))
mov=pickle.load(open('pickle/MovieList.pkl','rb'))
similarity = pickle.load(open('pickle/MovieSimilarityCoefficients.pkl','rb'))
duplicatequestion_model=pickle.load(open('pickle/DuplicateQuestionDetection_RandomForest.pkl','rb'))
duplicatequestion_cv=pickle.load(open('pickle/DuplicateQuestionDetection_CountVector.pkl','rb'))

clothes_model = ResNet50(weights='imagenet', include_top=False, input_shape=(224,224,3))
clothes_model.trainable = False
clothes_model = tensorflow.keras.Sequential([
    clothes_model,
    GlobalMaxPooling2D()
])

recommended_movie_names =["", "", "", "", ""]

# Home Page
@app.route("/", methods=['GET', 'POST'])
@app.route("/index.html", methods=['GET', 'POST'])
def home():
    return render_template('index.html')

# About Page
@app.route("/about.html", methods=['GET', 'POST'])
def about():
    return render_template('about.html')

# Applications Page
@app.route("/project.html", methods=['GET', 'POST'])
def project():
    return render_template('project.html')

# Contact Page
@app.route("/contactupdated.html", methods=['GET', 'POST'])
def contact():
    locations = sorted(house_data['location'].unique())
    return render_template('contactupdated.html', locations=locations)

@app.route("/404.html", methods=['GET', 'POST'])
def pagenotfound():
    return render_template('404.html')

@app.route("/comming-soon.html", methods=['GET', 'POST'])
def commingsoon():
    return render_template('comming-soon.html')

# Diabetes Price Prediction Page
@app.route("/diabetesprediction.html", methods=['GET', 'POST'])
def diabetes():
    return render_template('diabetesprediction.html')

@app.route("/predict_diabetes", methods=['POST'])
def predict_diabetes():
    glucose = float(request.form.get('glucose'))
    age = int(request.form.get('age'))
    bmi = float(request.form.get('bmi'))
    pregnancy = int(request.form.get('pregnancy'))
    pedigree = float(request.form.get('pedigree'))
    insulin = float(request.form.get('insulin'))
    bp = float(request.form.get('bp'))
    skinthickness = float(request.form.get('skinthickness'))

    # Formin the input dataset
    input = pd.DataFrame([[pregnancy, glucose, bp, skinthickness, insulin, bmi, pedigree, age]], columns=['Pregnancies', 'Glucose', 'BloodPressure', 'SkinThickness', 'Insulin',
       'BMI', 'DiabetesPedigreeFunction', 'Age'])

    print(input)

    NewBMI = pd.Series(["Underweight", "Normal", "Overweight", "Obesity 1", "Obesity 2", "Obesity 3"], dtype="category")
    input["NewBMI"] = NewBMI
    input.loc[input["BMI"] < 18.5, "NewBMI"] = NewBMI[0]
    input.loc[(input["BMI"] > 18.5) & (input["BMI"] <= 24.9), "NewBMI"] = NewBMI[1]
    input.loc[(input["BMI"] > 24.9) & (input["BMI"] <= 29.9), "NewBMI"] = NewBMI[2]
    input.loc[(input["BMI"] > 29.9) & (input["BMI"] <= 34.9), "NewBMI"] = NewBMI[3]
    input.loc[(input["BMI"] > 34.9) & (input["BMI"] <= 39.9), "NewBMI"] = NewBMI[4]
    input.loc[input["BMI"] > 39.9, "NewBMI"] = NewBMI[5]

    print(input)

    def set_insulin(row):
        if row["Insulin"] >= 16 and row["Insulin"] <= 166:
            return "Normal"
        else:
            return "Abnormal"

    input = input.assign(NewInsulinScore=input.apply(set_insulin, axis=1))
    if(input['NewInsulinScore'].iloc[0]=='Abnormal'):
        input['NewInsulinScore_Normal']=0
    else:
        input['NewInsulinScore_Normal'] = 1
    print(input)


    NewGlucose = pd.Series(["Low", "Normal", "Overweight", "Secret", "High"], dtype="category")
    input["NewGlucose"] = NewGlucose
    input.loc[input["Glucose"] <= 70, "NewGlucose"] = NewGlucose[0]
    input.loc[(input["Glucose"] > 70) & (input["Glucose"] <= 99), "NewGlucose"] = NewGlucose[1]
    input.loc[(input["Glucose"] > 99) & (input["Glucose"] <= 126), "NewGlucose"] = NewGlucose[2]
    input.loc[input["Glucose"] > 126, "NewGlucose"] = NewGlucose[3]

    input = pd.get_dummies(input, columns=["NewBMI", "NewGlucose"], drop_first = True)

    print(input)

    categorical_input = input[['NewBMI_Obesity 1', 'NewBMI_Obesity 2', 'NewBMI_Obesity 3', 'NewBMI_Overweight', 'NewBMI_Underweight', 'NewInsulinScore_Normal', 'NewGlucose_Low', 'NewGlucose_Normal', 'NewGlucose_Overweight', 'NewGlucose_Secret']]

    X = input.drop(['NewBMI_Obesity 1','NewBMI_Obesity 2', 'NewBMI_Obesity 3', 'NewBMI_Overweight','NewBMI_Underweight',
                     'NewInsulinScore_Normal','NewGlucose_Low','NewGlucose_Normal', 'NewGlucose_Overweight', 'NewGlucose_Secret', 'NewInsulinScore'], axis = 1)
    print(X)
    cols = X.columns
    index = X.index

    # transformer = RobustScaler().fit(X)
    # X = transformer.transform(X)

    X = pd.DataFrame(X, columns=cols, index=index)
    print(X)
    X = pd.concat([X, categorical_input], axis=1)


    result = diabetes_model.predict(X)[0]
    print(result)
    if result == 1:
        return "1"
    else:
        return "0"


# House Price Prediction Page
@app.route("/houseprice.html", methods=['GET', 'POST'])
def houseprice():
    locations = sorted(house_data['location'].unique())
    return render_template('houseprice.html', locations=locations)

@app.route("/predict_houseprice", methods=['POST'])
def predict_houseprice():
    location = request.form.get('locations')
    sqft = request.form.get('sqft')
    bath = request.form.get('bath')
    bhk = request.form.get('bhk')

    # print( location, sqft, bhk, bath )
    input= pd.DataFrame([[location, sqft, bath, bhk]], columns=['location', 'total_sqft', 'bath', 'bhk'])
    prediction=house_model.predict(input)[0] * 1e5
    return str(np.round(prediction, 2))

# Laptop Price Prediction Page
@app.route("/laptopprice.html", methods=['GET', 'POST'])
def laptopprice():
    companies = sorted(laptop_data['Company'].unique())
    types = sorted(laptop_data['TypeName'].unique())
    cpulist = sorted(laptop_data['Cpu brand'].unique())
    gpulist = sorted(laptop_data['Gpu brand'].unique())
    oslist= sorted(laptop_data['os'].unique())

    return render_template('laptopprice.html', companies=companies, types=types, cpulist=cpulist, gpulist=gpulist, oslist=oslist)

@app.route("/predict_laptopprice", methods=['POST', 'GET'])
def predict_laptopprice():
    company = request.form.get('company')
    type = request.form.get('type')
    ram = int(request.form.get('ram'))
    weight = float(request.form.get('weight'))
    touchscreen = request.form.get('touchscreen')
    ips = request.form.get('ips')
    screen_size = request.form.get('screensize')
    resolution = request.form.get('resolution')
    cpu = request.form.get('cpu')
    hdd = int(request.form.get('hdd'))
    ssd = int(request.form.get('ssd'))
    gpu = request.form.get('gpu')
    os = request.form.get('os')

    if touchscreen == 'Yes':
        touchscreen = 1
    else:
        touchscreen = 0

    if ips == 'Yes':
        ips = 1
    else:
        ips = 0

    X_res = int(resolution.split('x')[0])
    Y_res = int(resolution.split('x')[1])
    ppi = ((X_res ** 2) + (Y_res ** 2)) ** 0.5 / float(screen_size)
    query = np.array([company, type, ram, weight, touchscreen, ips, ppi, cpu, hdd, ssd, gpu, os])
    query = query.reshape(1, 12)

    return str(np.float32(np.exp(laptopprice_model.predict(query)[0])))

# Email Spam//Ham Page
@app.route("/emailSpamHam.html", methods=['GET', 'POST'])
def emailSpamHam():
    return render_template('emailSpamHam.html')

# Function to transform the input text into required features before testing on the trained saved model.
def transform_email_text(text):
    ps = PorterStemmer()
    text = text.lower()
    text = nltk.word_tokenize(text)
    y = []
    for i in text:
        if i.isalnum():
            y.append(i)
    text = y[:]
    y.clear()
    for i in text:
        if i not in stopwords.words('english') and i not in string.punctuation:
            y.append(i)
    text = y[:]
    y.clear()
    for i in text:
        y.append(ps.stem(i))
    return " ".join(y)

@app.route("/predict_email", methods=['GET', 'POST'])
def predict_email():
    msg = request.form.get('email_sms')
    # 1. preprocess
    transformed_msg = transform_email_text(msg)
    # 2. vectorize
    vector_input = emailspamham_tfidf.transform([transformed_msg])
    # 3. predict
    result = emailspamham_model.predict(vector_input)[0]
    # 4. Display
    if result == 1:
        return "Spam"
    else:
        return "Not Spam"

# Stock Price Prediction Page
@app.route("/stockprice.html", methods=['GET', 'POST'])
def stockprice():
    return render_template('stockprice.html')

@app.route("/predict_stockprice", methods=['GET', 'POST'])
def predict_stockprice():
    ticker = request.form.get('ticker')

    return '../static/img/stock_trends/'+ticker+'_2010-01-01_2022-06-26.png'

# Currency Conversion Chatbot Page
@app.route("/currencyconvertorchatbot.html")
def currencyconvertorchatbot():
    return render_template('currencyconvertorchatbot.html')

# Movie Recommendor Page
@app.route("/moviesrecommendor.html", methods=['GET', 'POST'])
def moviesrecommendor():
    movieList=mov['title'].values
    return render_template('moviesrecommendor.html', movieList=movieList)

@app.route("/recommend_movie", methods=['POST'])
def recommend_movie():
    selectedmovie = request.form.get('movielist')
    recommended_movie_names, recommended_movie_posters = recommend(selectedmovie)
    return jsonify({'recommended_movie_names': recommended_movie_names, 'recommended_movie_posters':recommended_movie_posters})

def recommend(movie):
    index = mov[mov['title'] == movie].index[0]
    distances = sorted(list(enumerate(similarity[index])), reverse=True, key=lambda x: x[1])
    recommended_movies = []
    recommended_poster = []
    for i in distances[1:6]:
        mov_id = mov.iloc[i[0]].movie_id
        recommended_movies.append(mov.iloc[i[0]].title)
        recommended_poster.append(fetch_poster(mov_id))
    return recommended_movies, recommended_poster

def fetch_poster(movie_id):
    url = "https://api.themoviedb.org/3/movie/{}?api_key=2fa688227dabc8d48fab8df6a04a6587&language=en-US".format(
        movie_id)
    data = requests.get(url)
    data = data.json()
    poster_path = data['poster_path']
    full_path = "https://image.tmdb.org/t/p/w500/" + poster_path
    return full_path


# Clothes Recommendor Page
@app.route("/clothesrecommendor.html", methods=['GET', 'POST'])
def clothesrecommendor():
    return render_template('clothesrecommendor.html')

# Feature Extraction for Clothes Image
def feature_extraction(img_path,clothes_model):
    img = image.load_img(img_path, target_size=(224, 224))
    img_array = image.img_to_array(img)
    expanded_img_array = np.expand_dims(img_array, axis=0)
    preprocessed_img = preprocess_input(expanded_img_array)
    result = clothes_model.predict(preprocessed_img).flatten()
    normalized_result = result / norm(result)

    return normalized_result

@app.route("/recommend_clothes", methods=['POST'])
def recommend_clothes():
    APP_ROOT = os.path.dirname(os.path.abspath(__file__))
    UPLOAD_FOLD = '/Users/krashi27/Documents/ML Projects/MLApplicationHub/datasets/uploaded_image'
    UPLOAD_FOLDER = os.path.join(APP_ROOT, UPLOAD_FOLD)
    app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

    f = request.files['file']
    file_path=os.path.join(app.config['UPLOAD_FOLDER'], secure_filename(f.filename))
    f.save(file_path)

    features=feature_extraction(file_path, clothes_model)

    neighbors = NearestNeighbors(n_neighbors=5, algorithm='brute', metric='euclidean')
    neighbors.fit(clothes_feature_list)

    distances, indices = neighbors.kneighbors([features])

    pathnames=[]
    for i in range(5):
        pathnames.append(clothes_images_filenames[indices[0][i]])

    return jsonify({'recommended_clothes': pathnames})

# Body Posture Detection Page
@app.route("/bodyposture.html")
def bodyposture():
    return render_template('bodyposture.html')


# Duplicate Question Detection Page
@app.route("/duplicatequesdetector.html")
def duplicatequesdetector():
    return render_template('duplicatequesdetector.html')

@app.route("/predict_duplicatequestions", methods=['POST'])
def predict_duplicatequestions():
    ques1 = request.form.get('ques1')
    ques2 = request.form.get('ques2')
    query = query_point_creator(ques1, ques2)
    result = duplicatequestion_model.predict(query)[0]
    if result:
        return 'Duplicate'
    else:
        return 'Not Duplicate'

def test_common_words(q1,q2):
    w1 = set(map(lambda word: word.lower().strip(), q1.split(" ")))
    w2 = set(map(lambda word: word.lower().strip(), q2.split(" ")))
    return len(w1 & w2)

def test_total_words(q1,q2):
    w1 = set(map(lambda word: word.lower().strip(), q1.split(" ")))
    w2 = set(map(lambda word: word.lower().strip(), q2.split(" ")))
    return (len(w1) + len(w2))

def test_fetch_token_features(q1, q2):
    SAFE_DIV = 0.0001

    STOP_WORDS = stopwords.words("english")

    token_features = [0.0] * 8

    # Converting the Sentence into Tokens:
    q1_tokens = q1.split()
    q2_tokens = q2.split()

    if len(q1_tokens) == 0 or len(q2_tokens) == 0:
        return token_features

    # Get the non-stopwords in Questions
    q1_words = set([word for word in q1_tokens if word not in STOP_WORDS])
    q2_words = set([word for word in q2_tokens if word not in STOP_WORDS])

    # Get the stopwords in Questions
    q1_stops = set([word for word in q1_tokens if word in STOP_WORDS])
    q2_stops = set([word for word in q2_tokens if word in STOP_WORDS])

    # Get the common non-stopwords from Question pair
    common_word_count = len(q1_words.intersection(q2_words))

    # Get the common stopwords from Question pair
    common_stop_count = len(q1_stops.intersection(q2_stops))

    # Get the common Tokens from Question pair
    common_token_count = len(set(q1_tokens).intersection(set(q2_tokens)))

    token_features[0] = common_word_count / (min(len(q1_words), len(q2_words)) + SAFE_DIV)
    token_features[1] = common_word_count / (max(len(q1_words), len(q2_words)) + SAFE_DIV)
    token_features[2] = common_stop_count / (min(len(q1_stops), len(q2_stops)) + SAFE_DIV)
    token_features[3] = common_stop_count / (max(len(q1_stops), len(q2_stops)) + SAFE_DIV)
    token_features[4] = common_token_count / (min(len(q1_tokens), len(q2_tokens)) + SAFE_DIV)
    token_features[5] = common_token_count / (max(len(q1_tokens), len(q2_tokens)) + SAFE_DIV)

    # Last word of both question is same or not
    token_features[6] = int(q1_tokens[-1] == q2_tokens[-1])
    # First word of both question is same or not
    token_features[7] = int(q1_tokens[0] == q2_tokens[0])
    return token_features

def test_fetch_length_features(q1, q2):
    length_features = [0.0] * 3

    # Converting the Sentence into Tokens:
    q1_tokens = q1.split()
    q2_tokens = q2.split()

    if len(q1_tokens) == 0 or len(q2_tokens) == 0:
        return length_features

    # Absolute length features
    length_features[0] = abs(len(q1_tokens) - len(q2_tokens))

    # Average Token Length of both Questions
    length_features[1] = (len(q1_tokens) + len(q2_tokens)) / 2

    strs = list(distance.lcsubstrings(q1, q2))
    length_features[2] = len(strs[0]) / (min(len(q1), len(q2)) + 1)
    return length_features


def test_fetch_fuzzy_features(q1, q2):
    fuzzy_features = [0.0] * 4

    # fuzz_ratio
    fuzzy_features[0] = fuzz.QRatio(q1, q2)

    # fuzz_partial_ratio
    fuzzy_features[1] = fuzz.partial_ratio(q1, q2)

    # token_sort_ratio
    fuzzy_features[2] = fuzz.token_sort_ratio(q1, q2)

    # token_set_ratio
    fuzzy_features[3] = fuzz.token_set_ratio(q1, q2)

    return fuzzy_features


def preprocess(q):
    q = str(q).lower().strip()

    # Replace certain special characters with their string equivalents
    q = q.replace('%', ' percent')
    q = q.replace('$', ' dollar ')
    q = q.replace('₹', ' rupee ')
    q = q.replace('€', ' euro ')
    q = q.replace('@', ' at ')

    # The pattern '[math]' appears around 900 times in the whole dataset.
    q = q.replace('[math]', '')

    # Replacing some numbers with string equivalents (not perfect, can be done better to account for more cases)
    q = q.replace(',000,000,000 ', 'b ')
    q = q.replace(',000,000 ', 'm ')
    q = q.replace(',000 ', 'k ')
    q = re.sub(r'([0-9]+)000000000', r'\1b', q)
    q = re.sub(r'([0-9]+)000000', r'\1m', q)
    q = re.sub(r'([0-9]+)000', r'\1k', q)

    # Decontracting words
    # https://en.wikipedia.org/wiki/Wikipedia%3aList_of_English_contractions
    # https://stackoverflow.com/a/19794953
    contractions = {
        "ain't": "am not",
        "aren't": "are not",
        "can't": "can not",
        "can't've": "can not have",
        "'cause": "because",
        "could've": "could have",
        "couldn't": "could not",
        "couldn't've": "could not have",
        "didn't": "did not",
        "doesn't": "does not",
        "don't": "do not",
        "hadn't": "had not",
        "hadn't've": "had not have",
        "hasn't": "has not",
        "haven't": "have not",
        "he'd": "he would",
        "he'd've": "he would have",
        "he'll": "he will",
        "he'll've": "he will have",
        "he's": "he is",
        "how'd": "how did",
        "how'd'y": "how do you",
        "how'll": "how will",
        "how's": "how is",
        "i'd": "i would",
        "i'd've": "i would have",
        "i'll": "i will",
        "i'll've": "i will have",
        "i'm": "i am",
        "i've": "i have",
        "isn't": "is not",
        "it'd": "it would",
        "it'd've": "it would have",
        "it'll": "it will",
        "it'll've": "it will have",
        "it's": "it is",
        "let's": "let us",
        "ma'am": "madam",
        "mayn't": "may not",
        "might've": "might have",
        "mightn't": "might not",
        "mightn't've": "might not have",
        "must've": "must have",
        "mustn't": "must not",
        "mustn't've": "must not have",
        "needn't": "need not",
        "needn't've": "need not have",
        "o'clock": "of the clock",
        "oughtn't": "ought not",
        "oughtn't've": "ought not have",
        "shan't": "shall not",
        "sha'n't": "shall not",
        "shan't've": "shall not have",
        "she'd": "she would",
        "she'd've": "she would have",
        "she'll": "she will",
        "she'll've": "she will have",
        "she's": "she is",
        "should've": "should have",
        "shouldn't": "should not",
        "shouldn't've": "should not have",
        "so've": "so have",
        "so's": "so as",
        "that'd": "that would",
        "that'd've": "that would have",
        "that's": "that is",
        "there'd": "there would",
        "there'd've": "there would have",
        "there's": "there is",
        "they'd": "they would",
        "they'd've": "they would have",
        "they'll": "they will",
        "they'll've": "they will have",
        "they're": "they are",
        "they've": "they have",
        "to've": "to have",
        "wasn't": "was not",
        "we'd": "we would",
        "we'd've": "we would have",
        "we'll": "we will",
        "we'll've": "we will have",
        "we're": "we are",
        "we've": "we have",
        "weren't": "were not",
        "what'll": "what will",
        "what'll've": "what will have",
        "what're": "what are",
        "what's": "what is",
        "what've": "what have",
        "when's": "when is",
        "when've": "when have",
        "where'd": "where did",
        "where's": "where is",
        "where've": "where have",
        "who'll": "who will",
        "who'll've": "who will have",
        "who's": "who is",
        "who've": "who have",
        "why's": "why is",
        "why've": "why have",
        "will've": "will have",
        "won't": "will not",
        "won't've": "will not have",
        "would've": "would have",
        "wouldn't": "would not",
        "wouldn't've": "would not have",
        "y'all": "you all",
        "y'all'd": "you all would",
        "y'all'd've": "you all would have",
        "y'all're": "you all are",
        "y'all've": "you all have",
        "you'd": "you would",
        "you'd've": "you would have",
        "you'll": "you will",
        "you'll've": "you will have",
        "you're": "you are",
        "you've": "you have"
    }

    q_decontracted = []

    for word in q.split():
        if word in contractions:
            word = contractions[word]

        q_decontracted.append(word)

    q = ' '.join(q_decontracted)
    q = q.replace("'ve", " have")
    q = q.replace("n't", " not")
    q = q.replace("'re", " are")
    q = q.replace("'ll", " will")

    # Removing HTML tags
    q = BeautifulSoup(q)
    q = q.get_text()

    # Remove punctuations
    pattern = re.compile('\W')
    q = re.sub(pattern, ' ', q).strip()

    return q

def query_point_creator(q1, q2):
    input_query = []

    # preprocess
    q1 = preprocess(q1)
    q2 = preprocess(q2)

    # fetch basic features
    input_query.append(len(q1))
    input_query.append(len(q2))

    input_query.append(len(q1.split(" ")))
    input_query.append(len(q2.split(" ")))

    input_query.append(test_common_words(q1, q2))
    input_query.append(test_total_words(q1, q2))
    input_query.append(round(test_common_words(q1, q2) / test_total_words(q1, q2), 2))

    # fetch token features
    token_features = test_fetch_token_features(q1, q2)
    input_query.extend(token_features)

    # fetch length based features
    length_features = test_fetch_length_features(q1, q2)
    input_query.extend(length_features)

    # fetch fuzzy features
    fuzzy_features = test_fetch_fuzzy_features(q1, q2)
    input_query.extend(fuzzy_features)

    # bow feature for q1
    q1_bow = duplicatequestion_cv.transform([q1]).toarray()

    # bow feature for q2
    q2_bow = duplicatequestion_cv.transform([q2]).toarray()

    return np.hstack((np.array(input_query).reshape(1, 22), q1_bow, q2_bow))


if __name__=="__main__":
     app.run(debug=True)