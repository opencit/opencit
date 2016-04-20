'''
Created on Mar 5, 2012

@author: jabuhacx
'''
import time
import random
import struct
import base64
import hmac
import hashlib

class MtWilsonHttpAuthorization(object):
    '''
    classdocs
    '''


    def __init__(self, clientId, secretKey):
        '''
        Constructor
        '''
        self.clientId = clientId
        self.secretKey = secretKey
        self.signatureMethod = "HMAC-SHA256"
    
    def getAuthorization(self, httpMethod, requestUrl, requestBody):
        '''
        Generates the HTTP Authorization header to include in the request
        '''
        s = SignatureInput()
        s.httpMethod = httpMethod
        s.absoluteUrl = requestUrl
        s.fromToken = base64.b64encode(self.clientId)
        s.nonce = base64.b64encode(self.nonce())
        s.body = requestBody or ""
        s.timestamp = time.strftime('%Y-%m-%dT%H:%M:%S')
        s.signatureMethod = self.signatureMethod
        content = s.toString()
        print "signing block:\n"+content+"\n----\n"
        signature = self.sign(content)
        realm = None
        authorization = "MtWilson " + self.headerParams(s, realm, signature)
        return authorization
    
    def nonce(self):
        """ The nonce consists of the current time in milliseconds since 1970 and 8 bytes of random data """
        nonce = struct.pack('!QQQ',  long(time.time()*1000), random.randint(0,2**32-1), random.randint(0,2**32-1))        
        return nonce

    def sign(self, content):
        signature = base64.b64encode(hmac.new(self.secretKey, msg=content, digestmod=hashlib.sha256).digest())
        return signature

    def headerParams(self, signatureInput, realm, signature):
        s = signatureInput
        inputs = (s.httpMethod, s.absoluteUrl, s.fromToken, s.nonce, s.signatureMethod, s.timestamp, realm, signature);
        labels = ("http_method", "uri",       "username", "nonce", "signature_method", "timestamp", "realm", "signature")
        errors = []
        params = []
        for i in range(len(inputs)):
            if inputs[i] != None and '\'' in inputs[i]: 
                errors.append("{0} contains quotes".format(labels[i]))
            if inputs[i] != None:
                params.append("{0}=\"{1}\"".format(labels[i], inputs[i]))
        if errors:
            raise Exception("Cannot create authorization header: "+", ".join(errors))
        return ", ".join(params)
        
class SignatureInput(object):

    def __init__(self):
        '''
        Constructor
        '''
        self.httpMethod = None
        self.absoluteUrl = None
        self.fromToken = None
        self.nonce = None
        self.body = None
        self.timestamp = None
        self.signatureMethod = None

    def toString(self):
        return "Request: {s.httpMethod} {s.absoluteUrl}\nFrom: {s.fromToken}\nTimestamp: {s.timestamp}\nNonce: {s.nonce}\nSignature-Method: {s.signatureMethod}\n\n{s.body}".format(s=self)
