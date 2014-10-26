#! /usr/bin/python2.7

class CoordinateConversion():
    
    def rasterToRobot(self, xCoordinate, yCoordinate):
        xMax = 1024 #Camera Resolution X
        xMin = 0
        yMax = 768  #Camera Resolution Y
        yMin = 0
        d1 =  50 #Start of where the camera view intersects the ground in cm
        d2 = 300 #end of cameras view on the ground in cm
        d3 =  25
        d4 =  25
        robotArray = []
        
        
        rasterX = xCoordinate
        rasterY = yCoordinate
        
        robotY = (((d1 - d2) / (xMax - xMin)) * rasterY + d2)
        robotX = ((d3 - d4) * ((rasterY - d1) / (d2 - d1)) * (rasterX - (xMax / 2) / (xMax / 2)) + d4)
        
        robotArray = [robotX, robotY]
        
        return robotArray