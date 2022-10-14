# 1. Importing Libraries
import numpy as np
import pandas as pd
import ast
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from nltk.stem.porter import PorterStemmer
import pickle


# 2. Importing Dataset
movies = pd.read_csv('../datasets/tmdb_5000_movies.csv')
credits = pd.read_csv('../datasets/tmdb_5000_credits.csv')

# 3. Merging the two dataframes into one
movies = movies.merge(credits,on='title')

# 4. Droping the unrequired columns from the dataframe
movies = movies[['movie_id','title','overview','genres','keywords','cast','crew']]

# 5. Data Preprocessing and Tag generation
# Dropping Null Columns
movies.dropna(inplace=True)

# Formatting the genre and keywords column
# The present format of genre column is list of dictionaries. We want to extract the name attribute and make a list of that.
def convert(text):
    L = []
    for i in ast.literal_eval(text):
        L.append(i['name'])
    return L

movies['genres'] = movies['genres'].apply(convert)
movies['keywords'] = movies['keywords'].apply(convert)

# Formatting the cast column
# The present format of cast column is list of dictionaries. We want to extract the name attribute of only 1st three actors and make a list of that.
def convert_cast(text):
    L = []
    counter = 0
    for i in ast.literal_eval(text):
        if counter < 3:
            L.append(i['name'])
        counter+=1
    return L

movies['cast'] = movies['cast'].apply(convert_cast)

# Formatting the crew column
# We want to extract only the name attribute from each dictionary where job is Director and form a list out of that.
def fetch_director(text):
    L = []
    for i in ast.literal_eval(text):
        if i['job'] == 'Director':
            L.append(i['name'])
    return L

movies['crew'] = movies['crew'].apply(fetch_director)

# Formatting the overview from string to list
movies['overview'] = movies['overview'].apply(lambda x:x.split())

# Removing spaces between strings in each list element of each column
def collapse(L):
    L1 = []
    for i in L:
        # replacing space with null
        L1.append(i.replace(" ",""))
    return L1

movies['cast'] = movies['cast'].apply(collapse)
movies['crew'] = movies['crew'].apply(collapse)
movies['genres'] = movies['genres'].apply(collapse)
movies['keywords'] = movies['keywords'].apply(collapse)

# Preparing TAGS column by concatenating list of string in the overview, genre, keywords, cast and crew columns. This will make TAGS column a paragraph of text.
movies['tags'] = movies['overview'] + movies['genres'] + movies['keywords'] + movies['cast'] + movies['crew']

new = movies.drop(columns=['overview','genres','keywords','cast','crew'])
new['tags'] = new['tags'].apply(lambda x: " ".join(x))
new['tags'] = new['tags'].apply(lambda x: x.lower())

# 6. Text Vectorization
cv = CountVectorizer(max_features=5000,stop_words='english')
vector = cv.fit_transform(new['tags']).toarray()
similarity = cosine_similarity(vector)

# 7. Applying Stemming
# This is done to remove different forms of same words. Example -
# ['loving','loved','loves']=['love','love','love']
# ['actual','actually']=['actual','actual']
ps=PorterStemmer()
def stem(text):
    y=[]
    for i in text.split():
        y.append(ps.stem(i))
    return " ".join(y)

new['tags']=new['tags'].apply(stem)


# 8. Recommendation
def recommend(movie):
    index = new[new['title'] == movie].index[0]
    distances = sorted(list(enumerate(similarity[index])),reverse=True,key = lambda x: x[1])
    for i in distances[1:6]:
        print(new.iloc[i[0]].title)


pickle.dump(new,open('../pickle/MovieList.pkl','wb'))
pickle.dump(similarity,open('../pickle/MovieSimilarityCoefficients.pkl','wb'))
