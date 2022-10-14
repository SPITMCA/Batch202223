import yfinance as yf
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from datetime import timedelta, date
from keras.models import load_model
from sklearn.preprocessing import MinMaxScaler

# list of stocks - GOOG, AAPL, SBIN.NS, TSLA
START = "2010-01-01"
END = date.today().strftime("%Y-%m-%d")
stock = 'TSLA'

#Dowloading stock data via Yahoo Finance
def load_data(ticker):
    data = yf.download(ticker, START, END)
    data.reset_index(inplace=True)
    return data

df=load_data(stock)
# print(df.head())

# Splitting Data into Training and Testing Frame
data=df['Close']
training_data=pd.DataFrame(data[0:int(len(df)*0.7)])
testing_data=pd.DataFrame(data[int(len(df)*0.7):])

# Scaling Down the data
scaler = MinMaxScaler(feature_range=(0, 1))
scaled_training_data = scaler.fit_transform(training_data)

# Loading the Pre-trained model
model=load_model('../pickle/APPL_stock_1jan2010_23jun2022_lstm_model.h5')

# Testing Part
past_100_days = training_data.tail(100)
final_testing_data=past_100_days.append(testing_data, ignore_index=True)
scaled_final_testing_data=scaler.fit_transform(final_testing_data)

x_test, y_test = [], []

for i in range(100,scaled_final_testing_data.shape[0]):
    x_test.append(scaled_final_testing_data[i-100:i])
    y_test.append(scaled_final_testing_data[i, 0])

x_test, y_test = np.array(x_test), np.array(y_test)

# Making Predictions
y_predictions=model.predict(x_test)

# Scaling Up
predicted_closing_price = scaler.inverse_transform(y_predictions)
original_closing_price = np.array(df['Close'])
original_closing_price=original_closing_price[len(df)-len(predicted_closing_price):]


# Future 30 days Prediction
x_input=x_test[len(x_test)-1:].reshape(1,-1)
temp_input=list(x_input)
temp_input=temp_input[0].tolist()

lst_output = []
n_steps = 100
i = 0
while (i < 30):

    if (len(temp_input) > 100):
        # print(temp_input)
        x_input = np.array(temp_input[1:])
        # print("{} day input {}".format(i, x_input))
        x_input = x_input.reshape(1, -1)
        x_input = x_input.reshape((1, n_steps, 1))
        # print(x_input)
        yhat = model.predict(x_input, verbose=0)
        # print("{} day output {}".format(i, yhat))
        temp_input.extend(yhat[0].tolist())
        temp_input = temp_input[1:]
        # print(temp_input)
        lst_output.extend(yhat.tolist())
        i = i + 1
    else:
        x_input = x_input.reshape((1, n_steps, 1))
        yhat = model.predict(x_input, verbose=0)
        # print(yhat[0])
        temp_input.extend(yhat[0].tolist())
        # print(len(temp_input))
        lst_output.extend(yhat.tolist())
        i = i + 1

# print(scaler.inverse_transform(lst_output))

def daterange(date1, date2):
    for n in range(int ((date2 - date1).days)+1):
        yield date1 + timedelta(n)

pred_dates=[]
start_dt = date.today()
end_dt = start_dt + timedelta(days=29)
for dt in daterange(start_dt, end_dt):
    pred_dates.append(dt)


# Visualization
plt.figure(figsize=(12,6))
plt.plot(df['Date'].tail(len(predicted_closing_price)), predicted_closing_price, 'b', label="Predicted Closing Price")
plt.plot(df['Date'].tail(len(original_closing_price)), original_closing_price, 'r', label="Original Closing Price")
plt.plot(pred_dates, scaler.inverse_transform(lst_output), 'g', label="Predicted Future 30 days Closing Price")
plt.title(stock+' Stock Price Prediction ')
plt.xlabel('Time in days')
plt.ylabel('Price in USD')
plt.legend()
# plt.show()
file_name = '../static/img/stock_trends/'+stock+'_'+START+'_'+END+'.png'
plt.savefig(file_name)
