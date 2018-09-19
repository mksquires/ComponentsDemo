# STK Components Demo Application Information

The intention of this small application is to showcase [STK Components](http://help.agi.com/AGIComponents/html/Welcome.htm) and
STK Web Visualization Library(SWVL)/Cesium ion. This application does not necessarily represent best practices.

## Requirements

* Java 1.8 or later
* Ant
* STK Components 2018r3 or later
* STK Web Visualization Library (SWVL) or Cesium ion to view sensor volumes
* An IDE such as Visual Studio Code or Eclipse

## Usage Instructions

To compile this sample application with Ant:

* Copy your AGI.Foundation.lic file into the src directory.
* Alter the .classpath file and build.xml to correspond to your Components installation directory.
* Run "ant package".  

The application will be compiled, packaged into a jar, and placed in the dist 
directory.  You can then double-click the CesiumDemo.jar file to run the 
application, or, simply run "ant run".

The application will run your selected scenario and output a czml file for use with Cesium/SWVL. 
If relevant to your scenario, you can use the included HelloWorld.html in place of the Cesium/SWVL build's 
included HelloWorld.html. This replacement HelloWorld html file contains a legend that will be overlayed on top of the globe and can be customized.

