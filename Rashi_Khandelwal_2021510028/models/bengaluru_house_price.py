import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression, Lasso, Ridge
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.compose import make_column_transformer
from sklearn.pipeline import make_pipeline
from sklearn.metrics import r2_score
import pickle



data=pd.read_csv('../datasets/Bengaluru_House_Data.csv')

# print(data.head())
# print(data.shape)
# print(data.info())

# for column in data.columns:
#     print(data[column].value_counts())
#     print("**"*20)

# OUTPUT
# Super built-up  Area    8790
# Built-up  Area          2418
# Plot  Area              2025
# Carpet  Area              87
# Name: area_type, dtype: int64
# ****************************************
# Ready To Move    10581
# 18-Dec             307
# 18-May             295
# 18-Apr             271
# 18-Aug             200
#                  ...
# 15-Aug               1
# 17-Jan               1
# 16-Nov               1
# 16-Jan               1
# 14-Jul               1
# Name: availability, Length: 81, dtype: int64
# ****************************************
# Whitefield                        540
# Sarjapur  Road                    399
# Electronic City                   302
# Kanakpura Road                    273
# Thanisandra                       234
#                                  ...
# Bapuji Layout                       1
# 1st Stage Radha Krishna Layout      1
# BEML Layout 5th stage               1
# singapura paradise                  1
# Abshot Layout                       1
# Name: location, Length: 1305, dtype: int64
# ****************************************
# 2 BHK         5199
# 3 BHK         4310
# 4 Bedroom      826
# 4 BHK          591
# 3 Bedroom      547
# 1 BHK          538
# 2 Bedroom      329
# 5 Bedroom      297
# 6 Bedroom      191
# 1 Bedroom      105
# 8 Bedroom       84
# 7 Bedroom       83
# 5 BHK           59
# 9 Bedroom       46
# 6 BHK           30
# 7 BHK           17
# 1 RK            13
# 10 Bedroom      12
# 9 BHK            8
# 8 BHK            5
# 11 BHK           2
# 11 Bedroom       2
# 10 BHK           2
# 14 BHK           1
# 13 BHK           1
# 12 Bedroom       1
# 27 BHK           1
# 43 Bedroom       1
# 16 BHK           1
# 19 BHK           1
# 18 Bedroom       1
# Name: size, dtype: int64
# ****************************************
# GrrvaGr    80
# PrarePa    76
# Sryalan    59
# Prtates    59
# GMown E    56
#            ..
# Amionce     1
# JaghtDe     1
# Jauraht     1
# Brity U     1
# RSntsAp     1
# Name: society, Length: 2688, dtype: int64
# ****************************************
# 1200    843
# 1100    221
# 1500    205
# 2400    196
# 600     180
#        ...
# 3580      1
# 2461      1
# 1437      1
# 2155      1
# 4689      1
# Name: total_sqft, Length: 2117, dtype: int64
# ****************************************
# 2.0     6908
# 3.0     3286
# 4.0     1226
# 1.0      788
# 5.0      524
# 6.0      273
# 7.0      102
# 8.0       64
# 9.0       43
# 10.0      13
# 12.0       7
# 13.0       3
# 11.0       3
# 16.0       2
# 27.0       1
# 40.0       1
# 15.0       1
# 14.0       1
# 18.0       1
# Name: bath, dtype: int64
# ****************************************
# 2.0    5113
# 1.0    4897
# 3.0    1672
# 0.0    1029
# Name: balcony, dtype: int64
# ****************************************
# 75.00     310
# 65.00     302
# 55.00     275
# 60.00     270
# 45.00     240
#          ...
# 351.00      1
# 54.10       1
# 80.64       1
# 32.73       1
# 488.00      1
# Name: price, Length: 1994, dtype: int64
# ****************************************

# print(data.isna().sum())

# Dropping unrequired columns
data.drop(columns=['area_type', 'availability', 'society', 'balcony'], inplace=True)


# Filling the missing values
# print(data['location'].value_counts())
data['location']=data['location'].fillna('Sarjapur Road')

# print(data['size'].value_counts())
data['size']=data['size'].fillna('2 BHK')

data['bath']=data['bath'].fillna(data['bath'].median())

data['bhk'] = data['size'].str.split().str.get(0).astype(int)


# Function to convert the ranges in total_sqft column
def convertRange(x):
    temp=x.split('-')
    if len(temp)==2:
        return (float(temp[0]) + float(temp[0]))/2
    try:
        return float(x)
    except:
        return None

data['total_sqft']=data['total_sqft'].apply(convertRange)

# print(data.head())
# print(data.shape)

data['price_per_sqft']=data['price']*100000 / data['total_sqft']

data['location']=data['location'].apply(lambda x : x.strip())

location_count=data['location'].value_counts()
location_count_less_10=location_count[location_count<=10]

data['location']=data['location'].apply(lambda x : 'other' if x in location_count_less_10 else x)

# Outlier detection and removal
data=data[((data['total_sqft']/data['bhk'])>=300)]
# data.describe()

def remove_outliers_sqrt(df):
    df_output=pd.DataFrame()
    for key, subdf in df.groupby('location'):
        mean = np.mean(subdf.price_per_sqft)
        std = np.std(subdf.price_per_sqft)

        gen_df = subdf[(subdf.price_per_sqft > (mean-std)) & (subdf.price_per_sqft <= (mean+std))]
        df_output = pd.concat([df_output, gen_df], ignore_index=True)

    return df_output

data = remove_outliers_sqrt(data)
# print(data.describe())

def remove_outliers_bhk(df):
    exclude_indices = np.array([])
    for location, location_df in df.groupby('location'):
        bhk_stats={}
        for bhk, bhk_df in location_df.groupby('bhk'):
            bhk_stats[bhk]={
                'mean': np.mean(bhk_df.price_per_sqft),
                'std': np.std(bhk_df.price_per_sqft),
                'count': bhk_df.shape[0]
            }

        for bhk, bhk_df in location_df.groupby('bhk'):
            stats=bhk_stats.get(bhk-1)
            if stats and stats['count']>5:
                exclude_indices= np.append(exclude_indices, bhk_df[bhk_df.price_per_sqft<(stats['mean'])].index.values)

    return  df.drop(exclude_indices, axis='index')

data=remove_outliers_bhk(data)

data.drop(columns=['size', 'price_per_sqft'], inplace=True)
# print(data)

# Cleaned Pre-processed Data
# print(data.head())
# data.to_csv('../datasets/Cleaned_Bengaluru_House_Data.csv')
# print(data.shape)

x=data.drop(columns=['price'])
y=data['price']

xtrain, xtest, ytrain, ytest = train_test_split(x, y, test_size=0.30, random_state=0)

print(xtrain.head())
print(ytrain.head())

print(xtrain.shape)
print(ytrain.shape)

# Applying LinearRegression
column_transform = make_column_transformer((OneHotEncoder(sparse=False), ['location']), remainder='passthrough')

scaler=StandardScaler()
lr_model=LinearRegression(normalize=True)
house_pipe=make_pipeline(column_transform, scaler, lr_model)
house_pipe.fit(xtrain, ytrain)
y_predict_lr=house_pipe.predict(xtest)

print("Linear Regression : ", r2_score(ytest, y_predict_lr))


# Applying Lasso
lasso_model=Lasso()
house_pipe=make_pipeline(column_transform, scaler, lasso_model)
house_pipe.fit(xtrain, ytrain)
y_predict_lasso=house_pipe.predict(xtest)

print("Lasso : ", r2_score(ytest, y_predict_lasso))


# Applying Ridge
ridge_model=Ridge()
house_pipe=make_pipeline(column_transform, scaler, ridge_model)
house_pipe.fit(xtrain, ytrain)
y_predict_ridge=house_pipe.predict(xtest)

print("Ridge : ", r2_score(ytest, y_predict_ridge))

pickle.dump(house_pipe, open('../pickle/BengaluruHousePrice_RidgeModel.pkl', 'wb'))


