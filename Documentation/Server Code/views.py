import sys,os
sys.path.append('/home/awitoomer123/stroller/calcRoute')
from django.http import HttpResponse
import OSMHandler as nx
import calculateBB as bb
import networkx
import categoriesJsonHandler as cjh
import math
import json
import copy
import requests
from difflib import SequenceMatcher
import logging
import time
from threading import Thread,Lock
import hmac
import hashlib
import base64
class coordinate:
    def __init__(self, x, y):
      self.x = x
      self.y = y
def calcRoute(fromLon,fromLat,toLon,toLat):
    startTime={"time":time.time()}
    facebookAuth={"access_token":"385796825186612|2AiZPjB7bDfgLKyYUDBX-6V6Qxs","facebook_secret":"0fab479f8f29bac91bd40e7129f5a545"}
    dig = hmac.new(facebookAuth["facebook_secret"].encode('utf-8'), msg=facebookAuth["access_token"].encode('utf-8'), digestmod=hashlib.sha256)
    facebookAuth["fb_proof"]=dig.hexdigest()
    googleDirectionURL={"url":"https://maps.googleapis.com/maps/api/directions/json?mode=walking&origin={0}&destination={1}&waypoints=optimize:true|{2}&key=AIzaSyAIEtqhx3HfArhy2nAlaY0x-HkXZz8v6Qw"}
    nominatimURL={"url":"https://nominatim.openstreetmap.org/reverse?lat={0}&lon={1}&zoom=16&format=jsonv2"}
    categoriesSummaryExample = {"attractions":[0,0,0,0],
                       "cafes":[0,0,0,0],
                       "culturalactivities":[0,0,0,0],
                       "food":[0,0,0,0],
                       "foodshops":[0,0,0,0],
                       "nightlife":[0,0,0,0],
                       "parks":[0,0,0,0],
                       "shops":[0,0,0,0],
                       "sightseeing":[0,0,0,0]
    }
    edgesList=[]
    final64Bboxes=[]
    noBboxEdges=(None,[])
    categoriesSummary={}
    #missedDict={"shouldHaveBeenScored":0,"wasScored":0}
    def toRad(Value):
    # Converts numeric degrees to radians
        return Value * math.pi/180
    def getDistanceFromLatLonInMeters(lat1,lon1,lat2,lon2):
        R = 6371# Radius of the earth in km
        dLat = toRad(lat2-lat1)
        dLon = toRad(lon2-lon1)
        a=math.sin(dLat/2) * math.sin(dLat/2) +math.cos(toRad(lat1)) * math.cos(toRad(lat2)) * math.sin(dLon/2) * math.sin(dLon/2)
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
        d = R * c # Distance in km
        return d*1000

    def calculateMiddleRadius(fromLon,fromLat,toLon,toLat):
        midLon=(fromLon+toLon)/2
        midLat=(fromLat+toLat)/2
        minRadius=getDistanceFromLatLonInMeters(fromLat,fromLon,toLat,toLon)/2
        return {'lon':midLon,'lat':midLat,'radius':minRadius}

    def categoryCheck(currentCategory):
        categoriesPath=cjh.getCategoryPath(currentCategory)
        with open('/home/awitoomer123/stroller/calcRoute/attractions') as f:
            attractions = f.read().splitlines()
        with open('/home/awitoomer123/stroller/calcRoute/cafes') as f:
            cafes = f.read().splitlines()
        with open('/home/awitoomer123/stroller/calcRoute/culturalactivities') as f:
            culturalactivities = f.read().splitlines()
        with open('/home/awitoomer123/stroller/calcRoute/food') as f:
            food = f.read().splitlines()
        with open('/home/awitoomer123/stroller/calcRoute/foodshops') as f:
            foodshops = f.read().splitlines()
        with open('/home/awitoomer123/stroller/calcRoute/nightlife') as f:
            nightlife = f.read().splitlines()
        with open('/home/awitoomer123/stroller/calcRoute/parks') as f:
            parks = f.read().splitlines()
        with open('/home/awitoomer123/stroller/calcRoute/shops') as f:
            shops = f.read().splitlines()
        with open('/home/awitoomer123/stroller/calcRoute/sightseeing') as f:
            sightseeing = f.read().splitlines()
        for category in categoriesPath:
            if category in attractions:
                return "attractions"
            if category in cafes:
                return "cafes"
            if category in culturalactivities:
                return "culturalactivities"
            if category in food:
                return "food"
            if category in foodshops:
                return "foodshops"
            if category in nightlife:
                return "nightlife"
            if category in parks:
                return "parks"
            if category in shops:
                return "shops"
            if category in sightseeing:
                return "sightseeing"

        return False
    def calculateScoreForFacebookEntry(ratingCount, rating, checkins):
        score=0
        multiplier=1
        if ratingCount<15:
            dummy=3
            if checkins>350000:
                multiplier+=5
                dummy=5
            elif checkins>100000:
                multiplier+=2
                dummy=4.5
            elif checkins>50000:
                multiplier+=1
                dummy=4
            elif checkins>10000:
                multiplier+=0.7
                dummy=3.5
            elif checkins>5000:
                multiplier+=0.4
            elif checkins>1000:
                multiplier+=0.3
            elif checkins>400:
                multiplier+=0.15
            score=dummy*multiplier
        elif(rating<=3):
            score=rating
        elif(rating<=4):
            if ratingCount>2000:
                multiplier+=0.5
            elif ratingCount>400:
                multiplier+=0.3
            elif ratingCount>200:
                multiplier+=0.1
            if checkins>350000:
                multiplier+=3.5
            elif checkins>100000:
                multiplier+=1.5
            elif checkins>50000:
                multiplier+=0.7
            elif checkins>10000:
                multiplier+=0.5
            elif checkins>5000:
                multiplier+=0.3
            elif checkins>1000:
                multiplier+=0.15
            elif checkins>400:
                multiplier+=0.1
            score=multiplier*rating
        elif(rating<=5):
            if ratingCount>2000:
                multiplier+=0.7
            elif ratingCount>400:
                multiplier+=0.4
            elif ratingCount>200:
                multiplier+=0.2
            if rating>=4.9:
                multiplier+=2
            if checkins>350000:
                multiplier+=5
            elif checkins>100000:
                multiplier+=2
            elif checkins>50000:
                multiplier+=1
            elif checkins>10000:
                multiplier+=0.7
            elif checkins>5000:
                multiplier+=0.4
            elif checkins>1000:
                multiplier+=0.3
            elif checkins>400:
                multiplier+=0.15
            score=multiplier*rating
        return score
    def GetIntersection(fDst1,fDst2,P1,P2,Hit):
        if ( (fDst1 * fDst2) >= 0.0):
            return False
        if ( fDst1 == fDst2):
            return False
        Hit["x"] = P1.x + (P2.x-P1.x) * ( -fDst1/(fDst2-fDst1) )
        Hit["y"] = P1.y + (P2.y-P1.y) * ( -fDst1/(fDst2-fDst1) )
        return True

    def InBox(Hit,B1,B2,Axis):
        if ( Axis==1 and Hit["y"] > B1.y and Hit["y"] < B2.y):
            return True
        if ( Axis==2 and Hit["x"] > B1.x and Hit["x"] < B2.x):
            return True
        return False

        #returns true if line (L1, L2) intersects with the box (B1, B2)
        #returns intersection point in Hit
    def CheckLineBox(B1,B2,L1,L2):
        Hit={"x":0,"y":0}
        if (L2.x < B1.x and L1.x < B1.x):
            return False
        if (L2.x > B2.x and L1.x > B2.x):
            return False
        if (L2.y < B1.y and L1.y < B1.y):
            return False
        if (L2.y > B2.y and L1.y > B2.y):
            return False
        if (L1.x > B1.x and L1.x < B2.x and L1.y > B1.y and L1.y < B2.y):
            return True
        if ( (GetIntersection( L1.x-B1.x, L2.x-B1.x, L1, L2, Hit) and InBox( Hit, B1, B2, 1 ))
          or (GetIntersection( L1.y-B1.y, L2.y-B1.y, L1, L2, Hit) and InBox( Hit, B1, B2, 2 ))
          or (GetIntersection( L1.x-B2.x, L2.x-B2.x, L1, L2, Hit) and InBox( Hit, B1, B2, 1 ))
          or (GetIntersection( L1.y-B2.y, L2.y-B2.y, L1, L2, Hit) and InBox( Hit, B1, B2, 2 ))):
            return True
        return False



    def isPointinBBOX(bBoxTuple,lat1,lon1):#(bbox looks like lonMin,latMin, lonMax,latMax)
        if( bBoxTuple[0] <= lon1 and lon1 <= bBoxTuple[2] and bBoxTuple[1] <= lat1 and lat1 <= bBoxTuple[3] ):
            return True
        return False
    def BboxDivide4(bBoxTuple):
        centLon = (bBoxTuple[0] + bBoxTuple[2])/2.0
        centLat = (bBoxTuple[1] + bBoxTuple[3])/2.0
        if(centLon -180 > 0):
            centLon -= 360
        elif (centLon + 180 < 0):
            centLon += 360
        if(centLat -90 > 0):
            centLat -= 180
        elif (centLat + 90 < 0):
            centLat += 180
        bbox0 = (bBoxTuple[0], bBoxTuple[1],centLon, centLat)
        bbox1 = (centLon, bBoxTuple[1],bBoxTuple[2], centLat)
        bbox2 = (bBoxTuple[0],  centLat,centLon, bBoxTuple[3])
        bbox3 = (centLon, centLat,bBoxTuple[2], bBoxTuple[3])
        return [bbox0,bbox1,bbox2,bbox3]


    def BBoxDivideAndManage(G,bBoxTuple):#(bbox looks like lonMin,latMin, lonMax,latMax)
        #logging.info("im in bbox Div and Manage, first bbox tuple is:"+str(bBoxTuple))
        first4=BboxDivide4(bBoxTuple)
        first16=[]
        for bbox in first4:
            currentBbox4=BboxDivide4(bbox)
            for innerBbox in currentBbox4:
                first16.append(innerBbox)
        for bbox in first16:
            currentBbox4=BboxDivide4(bbox)
            for innerBbox in currentBbox4:
                final64Bboxes.append((innerBbox,[]))
        #logging.info("this is final64 pre edges: "+str(final64Bboxes))
        for edgeLock in edgesList:
            u=edgeLock[0]["from"]
            v=edgeLock[0]["to"]
            uCoordinate=coordinate(G.node[u]["lon"],G.node[u]["lat"])
            vCoordinate=coordinate(G.node[v]["lon"],G.node[v]["lat"])
            flagFound=False
            for bboxAndList in final64Bboxes:#bboxAndList=((,,,),[(edge,lock),(edge,lock)])
                bottomLeftBbox=coordinate(bboxAndList[0][0],bboxAndList[0][1])
                topRightBbox=coordinate(bboxAndList[0][2],bboxAndList[0][3])
                if (CheckLineBox(bottomLeftBbox,topRightBbox,uCoordinate,vCoordinate)):
                    bboxAndList[1].append(({"from":u,"to":v},edgeLock[1]))
            if (not flagFound):
                noBboxEdges[1].append(({"from":u,"to":v},edgeLock[1]))

    def checkBboxEdgesAndUpdateScore(currLat,currLon,category,name,score,G,bboxAndList,lock):
        edgeFound=False
        for edgeLock in bboxAndList[1]:
            u=edgeLock[0]["from"]
            v=edgeLock[0]["to"]
            uLat= G.node[u]["lat"]
            uLon= G.node[u]["lon"]
            vLat= G.node[v]["lat"]
            vLon= G.node[v]["lon"]
            cond=not((currLat>uLat and currLat>vLat) or (currLat<uLat and currLat<vLat) or (currLon>uLon and currLon>vLon) or (currLon<uLon and currLon<vLon) )
            if cond:
                edgeFound=True
                #with lock:
                with edgeLock[1]:
                    #missedDict["wasScored"]+=1
                    if(score>categoriesSummary[u+"-"+v][category][0]):
                        categoriesSummary[u+"-"+v][category][0]=score
                        categoriesSummary[u+"-"+v][category][1]=name
                        categoriesSummary[u+"-"+v][category][2]=currLat
                        categoriesSummary[u+"-"+v][category][3]=currLon
                    #logging.info("name was:"+name+" first node lat,lon : "+str(G.node[str(u)]["lat"])+","+str(G.node[str(u)]["lon"])+"second node lat,lon: "+str(G.node[str(v)]["lat"])+","+str(G.node[str(v)]["lon"])+"graph street:"+edge["name"]+" address from FB:"+location["street"])
                    G.get_edge_data(u,v)['weight']+=score
                    #G[u][v]['weight'] += score
                    #logging.info("edge from graph is:"+str(G[u][v]))
                    #d['weight']+=score
                #unlock
        return edgeFound
    def updateGraph(lock,G,category,ratingCount, rating, checkins, name, location):
        score=calculateScoreForFacebookEntry(ratingCount, rating, checkins)
        #logging.info("business name:"+name+" business category:"+category)
        #with lock:
        #    missedDict["shouldHaveBeenScored"]+=1
        currLat=location["latitude"]
        currLon=location["longitude"]
        #i=0
        edgeFound=False
        for bboxAndList in final64Bboxes:#bboxAndList=((,,,),[(edge,lock),(edge,lock)])
            tempFlag=False
            if isPointinBBOX(bboxAndList[0],currLat,currLon):
                #if name=="Ob-La-Di":
                #    logging.info("obladi bbox is:"+str(bboxAndList[0]))
                tempFlag=checkBboxEdgesAndUpdateScore(currLat,currLon,category,name,score,G,bboxAndList,lock)
                if tempFlag:
                    edgeFound=True
                    #break

        if not edgeFound:
            checkBboxEdgesAndUpdateScore(currLat,currLon,category,name,score,G,noBboxEdges,lock)
    def facebookObjectHandler(parsedJsonEntry,G,lock):
        #if "street" not in parsedJsonEntry["location"]:
        #    return
        category= categoryCheck(parsedJsonEntry["category"])
        if not category:
            return
        updateGraph(lock,G,category,parsedJsonEntry["rating_count"],parsedJsonEntry["overall_star_rating"] if "overall_star_rating" in parsedJsonEntry else 0,parsedJsonEntry["checkins"],parsedJsonEntry["name"],parsedJsonEntry["location"])


    def facebookLocationsFunc(url,G,lock,i):
        #logging.info("got into facebookLocFunc:"+str(time.time()-startTime["time"]))
        r = requests.get(url)
        #logging.info("finished request"+str(time.time()-startTime["time"]))

        if (r.status_code == 200):
            parsedJson  = r.json()
            threadExists=False
            if 'paging' in parsedJson and i<8:
                thread = Thread(target = facebookLocationsFunc, args = (parsedJson["paging"]["next"],G,lock,i+1))
                threadExists=True
                thread.start()
            for i in range(len(parsedJson["data"])):
                facebookObjectHandler(parsedJson["data"][i],G,lock)
            if threadExists:
                thread.join()
                #logging.info("time passed after thread join:"+str(time.time()-startTime["time"]))
        else:
            return

    def getGraph(lat,lon,radiusKM):
        bboxTuple=bb.boundingBox(lat,lon,radiusKM)#(lonMin,latMin, lonMax,latMax)
        logging.info('{0},{1},{2},{3}'.format(bboxTuple[0],bboxTuple[1],bboxTuple[2],bboxTuple[3]))
        G= nx.read_osm(nx.download_osm(bboxTuple[0],bboxTuple[1],bboxTuple[2],bboxTuple[3]))
        for u,v,d in G.edges_iter(data=True):
            if(str(u)+"-"+str(v) not in categoriesSummary):
                categoriesSummary[str(u)+"-"+str(v)] = copy.deepcopy(categoriesSummaryExample)
            edgesList.append(({"from" : str(u),"to":str(v),"name" : d['data'].tags['name'],"id":d['data'].id},Lock()))

        return (G,bboxTuple)

    def facebookData(lon,lat,radius,G):
        url = "https://graph.facebook.com/search?q=&type=place&fields=rating_count,overall_star_rating,checkins,category,name,location&categories=['FITNESS_RECREATION','FOOD_BEVERAGE','ARTS_ENTERTAINMENT','SHOPPING_RETAIL']&center="+str(lat)+","+str(lon)+"&distance="+str(radius)\
                  +"&access_token="+facebookAuth["access_token"]+"&appsecret_proof="+facebookAuth["fb_proof"]\
                  +"&limit=100"
        #url = "https://graph.facebook.com/search?q=&type=place&fields=rating_count,overall_star_rating,checkins,category,name,location&categories=['FITNESS_RECREATION','FOOD_BEVERAGE','ARTS_ENTERTAINMENT','SHOPPING_RETAIL']&center="+str(lat)+","+str(lon)+"&distance="+str(radius)\
        #  +"&access_token="+facebookAuth["access_token"]\
        #  +"&limit=100"
        logging.info(url)
        lock=Lock()
        thread = Thread(target = facebookLocationsFunc, args = (url,G,lock,1))
        thread.start()
        thread.join()
        logging.info("time passed after last thread join:"+str(time.time()-startTime["time"]))
        maxWeight=0
        for u,v,d in G.edges_iter(data=True):
            if d["weight"]>maxWeight:
                maxWeight=d["weight"]
        for u,v,d in G.edges_iter(data=True):
            d["weight"]=maxWeight/d["weight"]

    def dijkstraGraph(G,fromLon,fromLat,toLon,toLat):
        #global googleDirectionURL
        returnDict={"url":None,"highlights":[]}
        fromNodeDict={"minDistance":sys.maxsize,"node":None}
        toNodeDict={"minDistance":sys.maxsize,"node":None}
        currentDistance=sys.maxsize
        for v, d in G.nodes(data=True):
            currentDistance=getDistanceFromLatLonInMeters(fromLat,fromLon,float(d["lat"]),float(d["lon"]))
            if(currentDistance<fromNodeDict["minDistance"]):
                fromNodeDict["minDistance"]=currentDistance
                fromNodeDict["node"]=v
            currentDistance=getDistanceFromLatLonInMeters(toLat,toLon,float(d["lat"]),float(d["lon"]))
            if(currentDistance<toNodeDict["minDistance"]):
                toNodeDict["minDistance"]=currentDistance
                toNodeDict["node"]=v
        #logging.info(networkx.shortest_path_length(G,source=fromNodeDict["node"],target=toNodeDict["node"],weight='length'))
        try:
            dijkstraSelectedNodeList=networkx.dijkstra_path(G,fromNodeDict["node"],toNodeDict["node"])
        except:
            seenDict={}
            lastEdge=sys.maxsize
            for edge in networkx.dfs_edges(G,fromNodeDict["node"]):
                if edge[0] not in seenDict:
                    seenDict[edge[0]]=1
                    lastEdge=edge[1]
                else:
                    G.add_edge(lastEdge,toNodeDict["node"],weight=100)
            G.add_edge(lastEdge,toNodeDict["node"],weight=100)
            dijkstraSelectedNodeList=networkx.dijkstra_path(G,fromNodeDict["node"],toNodeDict["node"])

        googleDirectionURL["url"]=googleDirectionURL["url"].format(str(fromLat)+","+str(fromLon),(str(toLat)+","+str(toLon)),"{2}")
        #fix highlights
        categoriesBlacklist=[]
        for j in range(4):
            maxScore=0
            nodeNode=None
            nodeCategory=None
            for i in range(len(dijkstraSelectedNodeList)-1):
                reverseFlag=False
                if str(dijkstraSelectedNodeList[i])+"-"+str(dijkstraSelectedNodeList[i+1]) in categoriesSummary:
                    currentDict=categoriesSummary[str(dijkstraSelectedNodeList[i])+"-"+str(dijkstraSelectedNodeList[i+1])]
                else:
                    if str(dijkstraSelectedNodeList[i+1])+"-"+str(dijkstraSelectedNodeList[i]) not in categoriesSummary:
                        continue
                    currentDict=categoriesSummary[str(dijkstraSelectedNodeList[i+1])+"-"+str(dijkstraSelectedNodeList[i])]
                    reverseFlag=True
                for key, value in currentDict.items():
                    if key in categoriesBlacklist:
                        continue
                    if(value[0]>maxScore):
                        maxScore=value[0]
                        if not reverseFlag:
                            nodeNode=str(dijkstraSelectedNodeList[i])+"-"+str(dijkstraSelectedNodeList[i+1])
                        else:
                            nodeNode=str(dijkstraSelectedNodeList[i+1])+"-"+str(dijkstraSelectedNodeList[i])
                        nodeCategory=key
            categoriesBlacklist.append(nodeCategory)
            if(maxScore==0):
                break
        #    logging.info("dict of nodenode:"+str(categoriesSummary[nodeNode]))
        #    logging.info("adding highlight:"+categoriesSummary[nodeNode][nodeCategory][1]+"first node lat,lon : "+str(G.node[nodeNode.split("-")[0]]["lat"])+","+str(G.node[nodeNode.split("-")[0]]["lon"])+"second node lat,lon: "+str(G.node[nodeNode.split("-")[1]]["lat"])+","+str(G.node[nodeNode.split("-")[1]]["lon"]))
            returnDict["highlights"].append({"category":nodeCategory,"name":categoriesSummary[nodeNode][nodeCategory][1],"lat":categoriesSummary[nodeNode][nodeCategory][2],"lon":categoriesSummary[nodeNode][nodeCategory][3]})
        #save best 23 nodes
        #logging.info("number of nodes in dijkstraSelectedNodeList before first run:"+str(len(dijkstraSelectedNodeList)))
        if (len(dijkstraSelectedNodeList)-2) >23:
            nodeWeights={}
            for i in range(len(dijkstraSelectedNodeList)-1):
                if i==0:
                    nodeWeights[dijkstraSelectedNodeList[i]]=G.get_edge_data(dijkstraSelectedNodeList[i],dijkstraSelectedNodeList[i+1])['weight']
                else:
                    nodeWeights[dijkstraSelectedNodeList[i]]=min(G.get_edge_data(dijkstraSelectedNodeList[i],dijkstraSelectedNodeList[i+1])['weight'],G.get_edge_data(dijkstraSelectedNodeList[i-1],dijkstraSelectedNodeList[i])['weight'])
            indexesToDelete=[]
            indexToContinue=0
            for i in range(len(dijkstraSelectedNodeList)-1):
                if(i<indexToContinue):
                    continue
                streetName= G.get_edge_data(dijkstraSelectedNodeList[i],dijkstraSelectedNodeList[i+1])['data'].tags['name']
                for j in range(i+1,len(dijkstraSelectedNodeList)-1):
                    if G.get_edge_data(dijkstraSelectedNodeList[j],dijkstraSelectedNodeList[j+1])['data'].tags['name']==streetName:
                        indexesToDelete.append(j)
                    else:
                        indexToContinue=j
            dijkstraSelectedNodeList=[i for j, i in enumerate(dijkstraSelectedNodeList) if j not in indexesToDelete]
        #logging.info("number of nodes in dijkstraSelectedNodeList after first run:"+str(len(dijkstraSelectedNodeList)))
        if (len(dijkstraSelectedNodeList)-2) >23:
            nodeWeightsLight={}
            for i in range(1,len(dijkstraSelectedNodeList)-1):
                nodeWeightsLight[dijkstraSelectedNodeList[i]]=nodeWeights[dijkstraSelectedNodeList[i]]
            sortedNodeWeightsList = [(k, nodeWeightsLight[k]) for k in sorted(nodeWeightsLight, key=nodeWeightsLight.get)]
            i=0
            tempList=[]
            for k, v in sortedNodeWeightsList:
                if i==23:
                    break
                tempList.append(k)
                i+=1
            for item in list(dijkstraSelectedNodeList):
                if item not in tempList:
                    dijkstraSelectedNodeList.remove(item)



        #logging.info("number of nodes in dijkstraSelectedNodeList after second run:"+str(len(dijkstraSelectedNodeList)))
        #for node in dijkstraSelectedNodeList:
        #    logging.info("node "+node+" weight:"+str(nodeWeights[node]))
        #fix URL
        for node in dijkstraSelectedNodeList:
            if node==dijkstraSelectedNodeList[0] or node==dijkstraSelectedNodeList[len(dijkstraSelectedNodeList)-1]:
                continue
            googleDirectionURL["url"]=googleDirectionURL["url"].replace("{2}",str(G.node[node]['lat'])+','+str(G.node[node]['lon'])+"|{2}")
        googleDirectionURL["url"]=googleDirectionURL["url"].replace("|{2}","")
        googleDirectionURL["url"]=googleDirectionURL["url"].replace("{2}","")
        returnDict["url"]=googleDirectionURL["url"]
        return json.dumps(returnDict)
    logging.info("started");
    locationCircle=calculateMiddleRadius(fromLon,fromLat,toLon,toLat)
    getGraphOutput=getGraph(locationCircle["lat"],locationCircle["lon"],locationCircle["radius"]/1000)
    logging.info("time passed after getGraph:"+str(time.time()-startTime["time"]))
    G=getGraphOutput[0]
    bBoxTuple=getGraphOutput[1]
    BBoxDivideAndManage(G,bBoxTuple)
    #logging.info("time passed after Bbox Div and Manage:"+str(time.time()-startTime["time"]))
    facebookData(locationCircle["lon"],locationCircle["lat"],locationCircle["radius"],G)
    logging.info("time passed after facebookData:"+str(time.time()-startTime["time"]))
    resultJson=dijkstraGraph(G,fromLon,fromLat,toLon,toLat)
    #logging.info("hit/miss dict is:"+str(missedDict))
    return resultJson

def index(request):
    fromLon=float(request.GET.get('fromLon', ''))
    fromLat=float(request.GET.get('fromLat', ''))
    toLon=float(request.GET.get('toLon', ''))
    toLat=float(request.GET.get('toLat', ''))
    return HttpResponse(calcRoute(fromLon,fromLat,toLon,toLat))
