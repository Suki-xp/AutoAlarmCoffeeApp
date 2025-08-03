#Bare bones structure of everything together

import requests
import time
from datetime import datetime, timedelta
    
def client_alarm(given_time):
    #This is the alarm structure that will trigger based on the rest of the code 
    alarm_time = datetime.strptime(given_time, "%H:%M")
    alarm_time_schedule = datetime.combine(datetime.now().date(), alarm_time.time())

    #Will turn on plug 5 minutes before alarm goes off
    wake_up_time = alarm_time - timedelta(minutes=5)
    wake_up_schedule = datetime.combine(datetime.now().date(), wake_up_time.time())
    
    #Comparison of the times
    current_time = datetime.now()
    if current_time < wake_up_schedule:
        with open("new.log", "a") as log_file:
            log_file.write(f"Still waiting for 5-minute mark before alarm, should go off at {wake_up_schedule}\n")
        time.sleep(5)
        
    else:
        with open("new.log", "a") as log_file:
            log_file.write(f"Starting machine now at {current_time}\n")
        
    #Setting up the send request to the server    
    url = "http://localhost:5000/trigger"
    payload = {"action": "turn_on", "alarm_time": given_time} 
    #THE PAYLOAD DATA INPUT COMMANDS CAN BE ADDED TO TO ACCOUNT FOR THE COMPLEXITY OF THE PROGRAM
    try:
        response = requests.post(url, json=payload)
        print(f"Trigger sent. Server response: {response.text}")
    except Exception as e:
        print(f"Response coludn't be processed due to {e}")
    
if __name__ == "__main__":
    string_type = "14:40"
    client_alarm(string_type)