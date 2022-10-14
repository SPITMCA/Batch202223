# Dowloads -
# pip install nltk
# nltk.download('punkt')

# Importing libraries
import numpy as np
import pandas as pd
import string
import nltk
from sklearn.preprocessing import LabelEncoder
from nltk.stem.porter import PorterStemmer
from nltk.corpus import stopwords
from sklearn.feature_extraction.text import CountVectorizer,TfidfVectorizer
from sklearn.model_selection import train_test_split
from sklearn.naive_bayes import GaussianNB,MultinomialNB,BernoulliNB
from sklearn.metrics import accuracy_score,confusion_matrix,precision_score
import pickle


# 1. Loading Dataset
df = pd.read_csv('../datasets/Emails_spam_ham.csv', encoding='latin-1')


# 2. Data Cleaning

# drop last 3 cols
df.drop(columns=['Unnamed: 2','Unnamed: 3','Unnamed: 4'],inplace=True)

# renaming the cols and mapping ham to 0 and spam to 1
df.rename(columns={'v1':'target','v2':'text'},inplace=True)
encoder = LabelEncoder()
df['target'] = encoder.fit_transform(df['target'])

# remove duplicates
df = df.drop_duplicates(keep='first')

# 3. Creating new feature
df['num_characters'] = df['text'].apply(len)

# 4. Data Preprocessing
# Lower case
# Tokenization
# Removing special characters
# Removing stop words and punctuation
# Stemming
def transform_text(text):
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

ps = PorterStemmer()
df['transformed_text'] = df['text'].apply(transform_text)


# 5. Model Building
tfidf = TfidfVectorizer(max_features=3000)
X = tfidf.fit_transform(df['transformed_text']).toarray()
y = df['target'].values

X_train,X_test,y_train,y_test = train_test_split(X,y,test_size=0.2,random_state=2)

mnb = MultinomialNB()
mnb.fit(X_train,y_train)
y_pred = mnb.predict(X_test)
# print(accuracy_score(y_test,y_pred))
# print(confusion_matrix(y_test,y_pred))
# print(precision_score(y_test,y_pred))

# Saving the model
pickle.dump(tfidf,open('../pickle/EmailSpamHam_tfidf_vectorizer.pkl','wb'))
pickle.dump(mnb,open('../pickle/EmailSpamHam_MultinominalNaiveBayesModel.pkl','wb'))

