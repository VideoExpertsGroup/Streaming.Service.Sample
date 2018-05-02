# VXG Sample BackEnd and FrontEnd

VXG sample backend and frontend is a template that can be used to implement solution based on VXG Cloud and/or VXG Streaming Server.

Read more about VXG Cloud and VXG Server - https://www.videoexpertsgroup.com/cloud-platform/

## Documentation 
The full SDK documentation can be found here - https://dashboard.videoexpertsgroup.com/docs3/.

## VXG Streaming Server and Test Backend and Test FrontEnd

<br>
<br>
<img src="http://www.videoexpertsgroup.com/git/testserver1.png" alt="testserver sample" >
<br>

## BackEnd 

Backend is a simple PHP server. 

Main features:
   - User management
       - Sign Up
       - Sign In
       - Sign Out
   - Channel management 
       - Create new channel for streaming from IP camera or Mobile camera
       - Delete channel
       - Get list of channels  

## Frontend 

Frontend is a simple application to stream video on server and play it on mobile phone.

Supported platforms: Android, iOS

Supported browsers: Chrome, Safary, FireFox, Edge, Internet Explorer. 
    
## Limitations
    
Mobile application gets the channel list from server and streams video in the first channel

Mobile application plays the first channel in the list and there is no option to select another channel 

One or more channels should be created initially on server before mobile application can stream video

iOS version is not available yet and will be uploaded soon
   
  
