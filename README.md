# Before Uploading the Code
+ Remove all the comments. (/\*([^*]|[\r\n]|(\*+([^*/]|[\r\n])))*\*+/|[ \t]*//.*)
+ Remove all the Debug messages.
+ Use Obfuscate Program
+ Convert into Unicode

# Release History
* 1.0.0
    * First Bot Created with basic tactic.
* 1.1.0
    * ADDED: Camper Job, Push ability
* 1.1.3 
    * ADDED: elay is a dick note
* 1.2
	* ADDED: AntiCamper Job, nearest wall method, minor fixes, forgot to add roy u r a dick note :-1:
* 1.2.1
	* ADDED: New Push Method,New Basic Tactic that involve 1 Capsuler 2 AntiCampers and 5 Campers
	* FIXED: AntiCamper distance less then 300
	* IMPORTANT: Fix tryPush
* 1.2.2
	* ADDED: nearestWall(MapObject obj) in class Engine, getMyCapsuler() 
	* CHANGED: AntiCamper Strategy has been changed. NEEDS TO BE REVIEWED
	* NOTE: nearestWall(MapObject obj) might need to be static, currently its not!

# Things to Fix / TODO by Priority
 + ~~Camper Job~~  - **FIXED**
 + IMPORTENT! Check for Campers - THE MOST IMPORTANT SHIT IN THIS GAME. 
 + Anti-Camper Tactic/Job! (IF THERE ARE CAMPERS). **Partially Done**
 + Priority to double push
 + Change the tryPush methods to be much better(Always push to the nearestWall) (Need to implement NearestWalltoObject)
 + Push away from city can push the Pirate into the Capsule
 + ~~Check for the closest Wall.~~ - **DONE**
 + Saver - pirate that follow the capsule holder and save him from other pirates
 + Check if we should push our pirate to give him a boost (we should) **Implemented in the new Anti-Camper**
 + Add Engine methods for Initiolize
 + Implemets A* or dijkStar - can be copied from AlonM

# License
MIT License

Copyright (c) 2017 roy reznik

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
