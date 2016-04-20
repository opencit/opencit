'''
Created on Mar 5, 2012

@author: jabuhacx
'''
import unittest
import MtWilsonHttpAuthorization
import httplib
import time
import datetime

class Test(unittest.TestCase):

    def setUp(self):
        self.server = "localhost:8080"
        self.url = "/AttestationService/resources"
        self.mtwilson = MtWilsonHttpAuthorization.MtWilsonHttpAuthorization("root","root")       
        pass


    def tearDown(self):
        pass

    def testDatetimeFormat(self):
        print time.strftime('%Y-%m-%dT%H:%M:%S')
        '''fromtimestamp(time.time())'''
        d = datetime.datetime.now().replace(microsecond=0)
        print d.isoformat('T')
        pass

    def testCreateAuthorizationHeader(self):
        print self.mtwilson.getAuthorization("GET", "/hello/world", None)
        pass

    def testGetHostsTrust(self):
        uripath = self.url+"/hosts/trust?hostName=10.1.71.103"  
        headers = {"Authorization":self.mtwilson.getAuthorization("GET", "http://"+self.server+uripath, None)}
        print headers.get("Authorization")
        h1 = httplib.HTTPConnection(self.server)
        h1.connect()
        h1.request("GET", uripath, None, headers)
        response = h1.getresponse()
        print "Response: {0}\n{1}".format(response.status, response.read())
        h1.close()
        pass


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()