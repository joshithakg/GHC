#GOLDEN HOUR COMMUTE

##OBJECTIVE

Develop a solution that is cost effective, easy to implement, immediately put to use, use maximum possible ways to save a life in golden hour (who are in the critical time limit to save their lives by taking them to a hospital) by removing all the traffic obstacles on their commutation to the hospital.

##PROBLEM STATEMENT
We cannot always predict our commutation time in the metropolitan cities, during peak hours it can become even worse. There are also other obstacles such as bad roads, rains, unusual wait time in signals, traffic jams that can increase the commutation time multifold.

Time is critical in an emergency situation especially in the golden hour, it is important to reach the hospital early to save a life.  Person who had a heart attack at home or office, someone critically injured in a roadside accident will fall under the golden hour time. Traffic delays to reach the hospital is the major issue during those situations.  

##SOLUTION

The solution to address multiple causes for traffic delays are below :

Best Route of commutation:  Given a source and destination, we will have to find the best route. Not just the shortest distance, other factors such as the number of signals and unexpected issues like traffic jams should also be accounted for. 
Our application will find the best route using a map solution provider that would account all these factors
Note: The helper of the patient must have our app on their phone to inItiate the golden hour commute
Regulating Traffic and Signals:  Keeping the traffic signals GREEN at the time of arrival will save some time. The traffic signals within the city in India are automatic with pre-configured time intervals but the traffic constables are also usually deployed where there is high traffic which is our main area of concern as well. 
Given the advantage of availability of traffic constables at the signals, our application will send a notification to them (through our APP) who are deployed at the signals in our emergency commutation route
The traffic constable can regulate the traffic according to our notification. They can manually control the signals in such a way the signals are GREEN when the ambulance arrives at that point. The traffic constable can also additionally help to regulate near by the traffic just in time before the ambulance arrives
The traffic constable can also alert other nearby constables (who may not be using our APP) using their walkie-talkie to ease the patient’s commutation further until the hospital.
Note: The traffic constables must have our app on their phone and enable location services. 
Co-Commuters: Generally the co-commuters get to know the emergency only when they hear the ambulance from its siren or see the ambulance only when it is behind them which happens only at the close moment and they either decide to rush forward or stop abruptly, which creates additional chaos sometimes, using our solution now the co-commuters can also help us as a voluntary action to easily save a life.
Our application can send a custom notification in advance to all of the active commuters on their path. By seeing this notification in advance, the other commuters can comfortably give way to the patients. 
Note: The co-commuters must have our app and provide access to their current location. 

##USERS:

This solution is driven by responsible citizens, by government staffs, hospitals and many more. Anyone who would like to make a little contribution to save a life can help do this easily. All they have to do is to install our application on their phone and enable access to their location services, they can turn on the location service probably only when they are commuting. Traffic police constables can be requested by the Government to use the app mandatorily. The school buses or the taxi services can also ask their drivers to use our app. 

##SCENARIO:

Let us assume, there is an accident on the road and an ambulance arrives. The driver of that ambulance has our app and initiates the golden hour commutation on our App by choosing the source (current) location and target (the hospital).

The App decides the best route of commutation using the map API and shows it to the driver and the driver starts the commutation .

App finds all active users (constables and commuters) on that path commuting in that direction and sends them a custom notification (with the estimated time of arrival to each of their location). 

Constables upon seeing the notification will regulate the traffic and control the signal as per the arrival time of the ambulance and thus ensure no or less ‘pause’ time of the ambulance.

Other commuters can go off in a different path if the ambulance is far away or park their vehicles when the ambulance is coming closer. 

Constables can additionally contribute to regulating the traffic further using their alkie-talkies. 

Thus the ambulance can reach the hospital earlier and save the life. 

Hospital staff can also plan appropriately as per the arrival and be ready with the doctors and other resources.

##MAJOR FUNCTIONALITIES
Continuously identifying active users on the route and sending them a periodic notification -
Using location services, App can determine the current location of every active user and when a commutation is planned it will find the users on that route accordingly.  
The challenge may be to find the direction user is commuting as we do not have to alert users coming in the opposite direction 
Note this has to be happening continuously as the people can change their directions any time, so current commuters will go off the emergency and new commuters can come in. 
Map API for the requestor - Passing source and destination inputs to map provider, Get shortest path and Show the Map.
Map API for every user - Co-commuters can also locate the ambulance in a map on our App thus they know exactly when to assist the traffic in additiona to the notification that they receive 
Authenticity of the users who initiate the commutation - Either only trusted people like registered hospitals and its ambulance drivers will have the options to initiate the request but ideally everyone should be able to initiate (like a family member who is taking the patient had a heart attack) so we will have a way to quickly verify with the initiator and confirm in app (may be hospital can do this)
Multiple commutations - App can handle multiple emergency commutations.

