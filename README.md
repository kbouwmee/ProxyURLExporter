# Find APIs deployed on a Virtual Host in Apigee

This Java program finds all API Proxies deployed on a specific virtual host in an org and env.

## Dependencies
This Java program depends on the `org.json` library. I found mine here: [https://github.com/stleary/JSON-java](https://github.com/stleary/JSON-java) or use the [direct link](https://repo1.maven.org/maven2/org/json/json/20201115/json-20201115.jar).

## Config File
You need a config file that looks like this. Please update or create this file `myconfig.properties`. It should have the following attributes:
```
apigee.org=YOURORG
apigee.env=YOURENV
apigee.vhost=YOURVHOST
apigee.token=YOURTOKEN
```
The token is a valid OAuth token for the Apigee management APIs. See here on how to get it: [https://docs.apigee.com/api-platform/system-administration/using-oauth2#get-the-tokens](https://docs.apigee.com/api-platform/system-administration/using-oauth2#get-the-tokens) 

## Usage
Compile the Java program:
```
javac ProxyURLExporter.java -cp org.json.jar:.
```
This is how to run it:
```
java -cp org.json.jar:. ProxyURLExporter <configfile>
```
For example:
```
java -cp org.json.jar:. ProxyURLExporter myconfig.properties
```

