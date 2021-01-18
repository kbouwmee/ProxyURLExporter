// Copyright 2018-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.*;
import java.io.InputStream;
import java.util.zip.*;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.*;
import org.xml.sax.InputSource;

public class VirtualHostDownloader {

    private String org;
    private String env;
    private String vhostName;
    private String token;

    public VirtualHostDownloader(String org, String env, String vhost, String token) {
        this.org = org;
        this.env = env;
        this.vhostName = vhost;
        this.token = token;
    }

    private JSONObject getVirtualHost() {
        String requestUrl = "https://api.enterprise.apigee.com/v1/organizations/"+org+"/environments/"+env+"/virtualhosts/"+vhostName;
        JSONObject o = new JSONObject();
        try {
            // query management API to het all deployed API Proxies
            URL url = new URL(requestUrl);    
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Authorization", "Bearer "+token);
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");

            int responseCode = con.getResponseCode();
            //System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) 
            { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                o = new JSONObject(response.toString());
            } else if(responseCode == 401) {
                System.out.println("ERROR: You are unauthorized. Please check the access token in the config file you provided. Is could be missing or timed out.");
                //return null;
            } else if(responseCode == 404) {
                System.out.println("ERROR: the Org, Env, Vhost you provided cannot be found.");
                //return null;
            } else {
                System.out.println("GET request failed: " + responseCode);
                //return null;
            }
            
        } catch (Exception e)  {
            e.printStackTrace();
        }
        return o;
    }

    private VirtualHost parseVHost(JSONObject o) 
    {      
        ArrayList<String> haList;
        int port;
        boolean isSSLenabled;

        VirtualHost vh = null;

        try {
            JSONArray hostAliasesArray = o.getJSONArray("hostAliases");
            haList = new ArrayList<String>();
            for (int i = 0; i < hostAliasesArray.length(); i++)
            {
                haList.add(hostAliasesArray.getString(i));
            }
            // get Port number
            port = Integer.parseInt(o.getString("port"));

            // get SSL enablement
            JSONObject ssl = o.getJSONObject("sSLInfo");
            isSSLenabled = false;
            if(ssl.getString("enabled").equals("true")) isSSLenabled = true;

            vh = new VirtualHost(vhostName, haList, port, isSSLenabled);
        }
        catch (JSONException e) {
            System.out.println("JSON response from Apigee is not complete.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return vh;
    }

    public VirtualHost process() 
    {
        // get all api proxies deployed
        VirtualHost vhost = parseVHost(getVirtualHost());
        return vhost;
    }
}