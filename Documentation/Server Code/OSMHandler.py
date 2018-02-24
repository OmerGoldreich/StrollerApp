"""
Read graphs in Open Street Maps osm format

Based on osm.py from brianw's osmgeocode
http://github.com/brianw/osmgeocode, which is based on osm.py from
comes from Graphserver:
http://github.com/bmander/graphserver/tree/master and is copyright (c)
2007, Brandon Martin-Anderson under the BSD License
"""


import xml.sax
import copy
from math import radians, cos, sin, asin, sqrt
import logging
import networkx
import resource
import sys



def haversine(lon1, lat1, lon2, lat2, unit_m = True):
    """
    Calculate the great circle distance between two points
    on the earth (specified in decimal degrees)
    default unit : km
    """
    # convert decimal degrees to radians
    lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])

    # haversine formula
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * asin(sqrt(a))
    r = 6371 # Radius of earth in kilometers. Use 3956 for miles
    if (unit_m):
        r *= 1000
    return c * r


def download_osm(left,bottom,right,top):
    """ Return a filehandle to the downloaded data."""
    from urllib.request import urlopen
    #logging.info("BBOX is:left: {0},bottom: {1},right: {2},top: {3}".format(left,bottom,right,top))
    #fp = urlopen( "http://api.openstreetmap.org/api/0.6/map?bbox=%f,%f,%f,%f"%(left,bottom,right,top) )
    fp = urlopen( "http://www.overpass-api.de/api/xapi?way[highway=*][name=*][bbox=%f,%f,%f,%f]"%(left,bottom,right,top) )
    if(fp.getcode()!=200):
        logging.info("osm obtain failed")
    return fp
def read_osm(filename_or_stream):
    """Read graph in OSM format from file specified by name or by stream object.

    Parameters
    ----------
    filename_or_stream : filename or stream object

    Returns
    -------
    G : Graph

    Examples
    --------
    >>> G=nx.read_osm(nx.download_osm(-122.33,47.60,-122.31,47.61))

    """
    osm = OSM(filename_or_stream)
    G = networkx.Graph()

    for w in osm.ways.values():
        #if not ('highway' in w.tags and 'name' in w.tags):
        #    continue
        G.add_path(w.nds, id=w.id,weight=1,data=w)
    for n_id in G.nodes_iter():
        n = osm.nodes[n_id]
        G.node[n_id] = dict(data=n)
        G.node[n_id]['lat'] = n.lat
        G.node[n_id]['lon'] = n.lon
        G.node[n_id]['id'] = n.id
     ## Estimate the length of each way
    #logging.info("before haversing and length")
    #for u,v,d in G.edges_iter(data=True):
    #    distance = haversine(G.node[u]['lon'], G.node[u]['lat'], G.node[v]['lon'], G.node[v]['lat'], unit_m = True) # Give a realistic distance estimation (neither EPSG nor projection nor reference system are specified)
    #    G.add_weighted_edges_from([( u, v, distance)], weight='length')
    #    #logging.info(d['data'].tags['name'])
    #logging.info("after haversin and length")
    return G


class Node:
    def __init__(self, id, lon, lat):
        self.id = id
        self.lon = lon
        self.lat = lat
        self.tags = {}

class Way:
    def __init__(self, id, osm):
        self.osm = osm
        self.id = id
        self.nds = []
        self.tags = {}

    def split(self, dividers):

        resource.setrlimit(resource.RLIMIT_STACK, [0x10000000, resource.RLIM_INFINITY])
        sys.setrecursionlimit(0x100000)

        # slice the node-array using this nifty recursive function
        def slice_array(ar, dividers):
            for i in range(1,len(ar)-1):
                if dividers[ar[i]]>1:
                    #print "slice at %s"%ar[i]
                    left = ar[:i+1]
                    right = ar[i:]

                    rightsliced = slice_array(right, dividers)

                    return [left]+rightsliced
            return [ar]

        slices = slice_array(self.nds, dividers)

        # create a way object for each node-array slice
        ret = []
        i=0
        for slice in slices:
            littleway = copy.copy( self )
            littleway.id += "-%d"%i
            littleway.nds = slice
            ret.append( littleway )
            i += 1

        return ret




class OSM:
    preSplitWays={}
    def __init__(self, filename_or_stream):
        """ File can be either a filename or stream/file object."""
        nodes = {}
        ways = {}

        superself = self

        class OSMHandler(xml.sax.ContentHandler):
            @classmethod
            def setDocumentLocator(self,loc):
                pass

            @classmethod
            def startDocument(self):
                pass

            @classmethod
            def endDocument(self):
                pass

            @classmethod
            def startElement(self, name, attrs):
                if name=='node':
                    self.currElem = Node(attrs['id'], float(attrs['lon']), float(attrs['lat']))
                elif name=='way':
                    self.currElem = Way(attrs['id'], superself)
                elif name=='tag':
                    self.currElem.tags[attrs['k']] = attrs['v']
                elif name=='nd':
                    self.currElem.nds.append( attrs['ref'] )

            @classmethod
            def endElement(self,name):
                if name=='node':
                    nodes[self.currElem.id] = self.currElem
                elif name=='way':
                    ways[self.currElem.id] = self.currElem

            @classmethod
            def characters(self, chars):
                pass
        xml.sax.parse(filename_or_stream, OSMHandler)

        self.nodes = nodes
        self.ways = ways
        OSM.preSplitWays=self.ways
        #count times each node is used
        node_histogram = dict.fromkeys( self.nodes.keys(), 0 )
        for way in self.ways.values():
            if len(way.nds) < 2:       #if a way has only one node, delete it out of the osm collection
                del self.ways[way.id]
            else:
                for node in way.nds:
                    node_histogram[node] += 1

        #use that histogram to split all ways, replacing the member set of ways
        #logging.info("before osm parsing:")
        new_ways = {}
        for id, way in self.ways.items():
            split_ways = way.split(node_histogram)
            for split_way in split_ways:
                new_ways[split_way.id] = split_way
        self.ways = new_ways
        #logging.info("after osm parsing:")
