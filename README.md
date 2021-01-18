# Find APIs deployed on a Virtual Host in Apigee

This Java program finds all API Proxies deployed on a specific virtualhost in an org and env. It calls the Apigee Management API to find all deployed API Proxies in an environment and then downloads the ZIP files of each one to inspect the virtual host. It outputs a list of API Proxies, with deployed revision number on the requested virtualhost and it's basepath. Without this, you would have to inspect each API Proxy in the UI of Apigee.

## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.

## License

This material is copyright 2018-2020, Google LLC.
and is licensed under the Apache 2.0 license. See the [LICENSE](LICENSE) file.

This code is open source but you need to compile it in order to use it.

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

