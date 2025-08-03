import requests
import time
from flask import Flask, request
import asyncio
import time
from datetime import datetime, timedelta
from kasa.iot import IotPlug
import schedule
import threading
import os

app = Flask(__name__)

#Turning on command for the plug
async def turn_on_plug(ip):
    plug = IotPlug(ip)
    await plug.update()
    with open("new.log", "a") as log_file:
        log_file.write(f"Turning on plug at {time.strftime("%H:%M")}\n")
    await plug.turn_on()

#The turning off command for the plug
async def turn_off_plug(ip):
    plug = IotPlug(ip)
    await plug.update()
    with open("new.log", "a") as log_file:
        log_file.write(f"Turning off plug at {time.strftime("%H:%M")}\n")
    await plug.turn_off()
    
@app.route("/trigger", methods=["POST"])
def running_program():
    data = request.get_json()
    address = "192.168.1.181"
    
    with open("new.log", "a") as log_file:
        log_file.write(f"Received trigger: {data} at {time.strftime('%H:%M:%S')}\n")
    
    #The command that basically checks to see if the data that was parsed in by the json 
    #matches the payload, bascially if got the correct get request from the server
    if data and data.get("action") == "turn_on" and data.get("alarm_time"):
        #This is the command where it turns off 15 minutes minutes after the alarm has gone off
        #So that the coffee is nice and fresh
        #This is the alarm structure that will trigger based on the rest of the code 
        alarm_time_str = data.get("alarm_time") #Added with the additional data parsed to the payload
        alarm_time = datetime.strptime(alarm_time_str, "%H:%M")
        alarm_time_schedule = datetime.combine(datetime.now().date(), alarm_time.time())
        
        #This triggers the alarm to turn on the plug which startes 5 minutes before
        on_time = alarm_time_schedule - timedelta(minutes=5)

        #After 15 minutes turn off the alarm
        off_time = alarm_time_schedule + timedelta(minutes=15) 
        
        if on_time < datetime.now():
            on_time += timedelta(days=1)
            off_time += timedelta(days=1)
            with open ("new.log", "a") as log_file:
                log_file.write(f"Setting time for tommorow instead for {on_time.strftime('%H:%M')} and {off_time.strftime('%H:%M')} \n")
        
        schedule.every().day.at(on_time.strftime("%H:%M")).do(lambda: asyncio.run(turn_on_plug(address)))
        schedule.every().day.at(off_time.strftime("%H:%M")).do(lambda: asyncio.run(turn_off_plug(address)))
        #THIS IS THE COMMAND TO TRIGGER THE PLUG TO TURN OFF AFTER THE ALARM TIME FRAME 
    
        return {"status": "success"}, 200
    else:
        return {"status": "failure"}
    
#Need to update the code to include a cancel failsafe which will be in this call here
@app.route("/cancel", methods=["POST"])
def cancel_program():
    request.get_json()
    schedule.clear()
    with open("new.log", "a") as log_file:
            log_file.write(f"Cancelled the plug timing at {time.strftime('%H:%M:%S')}\n")
    return {"status": "success"}, 200
            
def running_command():
    while True:
        schedule.run_pending()
        time.sleep(5)
        
if __name__ == "__main__":
    scheduler_thread = threading.Thread(target=running_command, daemon=True)
    scheduler_thread.start()
    
    app.run(host="0.0.0.0", port=5000)
    #Creates the server to run on