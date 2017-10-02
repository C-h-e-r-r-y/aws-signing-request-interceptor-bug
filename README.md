# About repository

This repo contains a maven project with 2 java classes NonWorkingExampleStreamVersion, WorkingExampleNonStreamVersion.
Both of them has main method. Both take file from s3 and try to store it in aws elasticsearch. But:

 1. WorkingExampleNonStreamVersion - load whole file content into byte stream, creates base64 from it and then send file
  to elasticsearch
 1. NonWorkingExampleStreamVersion - do the same thins as previous but instead of downloading whole file it wrap stream 
 and uploads bytes to elaticsearch only when those byte arrives from s3. This version is does **not** work due to:
 
````
    {
      "message": "The request signature we calculated does not match the signature you provided. Check your AWS Secret Access Key and signing method. Consult the service documentation for details.
    }
````

So this means that something wrong with signing. :(

# The project uses
 1. aws sdk java2
 1. Jest client  `5.3.2`. Elasticsearch server version is the same.
 
# How to run

 1. First of all maven here is used as dependency manager only. I have no time to configurate runnable jar. So just
open project in IDE (I used Intellij Idea) and run classes from IDE.
 1. Before run you have to set the following environment variables:
    * AWS_ACCESS_KEY_ID - aws access key
    * AWS_SECRET_ACCESS_KEY - aws secret key 
    * ElasticSearchHost - host to eleasticsearch 
    * ElasticSearchPort - eleasticsearch port (443 or 80, aws does **not** support 9200 or 9300) 
    * ElasticSearchProtocol - http or https 
    * ApplicationRegion - aws region 
    * myIndex - elastisearch index to store file from s3 
    * myType - elastisearch type to store file from s3 
    * myId - id for storing in elastic search  
    * bucket - bucket for downloading s3 object 
    * s3Key - key for downloading s3 object
    
   