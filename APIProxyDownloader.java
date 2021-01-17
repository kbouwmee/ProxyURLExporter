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

public class APIProxyDownloader {

    private String org;
    private String env;
    private String token;

    public APIProxyDownloader(String org, String env, String token) {
        this.org = org;
        this.env = env;
        this.token = token;
    }

    private JSONObject getAPIProxyDeployments() {
        String requestUrl = "https://api.enterprise.apigee.com/v1/organizations/"+org+"/deployments";
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
                System.out.println("ERROR: the Org you provided cannot be found.");
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

    private ArrayList<APIProxy> parseAPIs(JSONObject o) 
    {      
        ArrayList<APIProxy> apis = new ArrayList<APIProxy>();
       
        try {
            JSONArray envArray = o.getJSONArray("environment");
            for (int i = 0; i < envArray.length(); i++)
            {
                // parse the JSON to extract all API Proxies (name, deployed revision and basepath)
                String env = envArray.getJSONObject(i).getString("name");
                if(env.equals(this.env)) {
                    // parse all deployed APIs in this env
                    JSONArray apiArray = envArray.getJSONObject(i).getJSONArray("aPIProxy");
                    
                    for (int j = 0; j < apiArray.length(); j++)
                    {
                        String APIProxyName = apiArray.getJSONObject(j).getString("name");
                        //System.out.println(APIProxyName);
                        JSONArray revArray = apiArray.getJSONObject(j).getJSONArray("revision");
                    
                        for (int k = 0; k < revArray.length(); k++) {
                            String deployedRevision = revArray.getJSONObject(k).getString("name");
                            String basePath = revArray.getJSONObject(k).getJSONObject("configuration").getString("basePath");
                            // initialize API Proxy object
                            APIProxy api = new APIProxy(APIProxyName, deployedRevision, basePath);
                            apis.add(api);
                        }
                    }
                }
            }
           
        }
        catch (JSONException e) {
            System.out.println("JSON response from Apigee is not complete.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return apis;
    }

    private Document getAPIProxyXML(APIProxy api) 
    {
        String requestUrl = "https://api.enterprise.apigee.com/v1/organizations/"+org+"/apis/"+api.name+"/revisions/"+api.deployedRevision+"?format=bundle";
        String arrayString = "";
        Document document = null;
        try {
            // Open management API to download zip with XML configuration of the API proxy
            URL url = new URL(requestUrl);    
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer "+token);

            int responseCode = con.getResponseCode();
            // System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) 
            { // success                                
                // open input stream from the HTTP connection
                InputStream fis = con.getInputStream();
                // unzip the API Proxy
                ZipInputStream zis = new ZipInputStream(fis);
                StringBuilder s = new StringBuilder();
                byte[] buffer = new byte[1024];
                int read = 0;
                ZipEntry entry;
                while ((entry = zis.getNextEntry())!= null) {
                    String fileName = entry.getName().trim();
                    // we are only interested in the proxy endpoint xml file
                    if (fileName.contains("apiproxy/proxies/") && fileName.endsWith(".xml")) 
                    {
                        while ((read = zis.read(buffer, 0, 1024)) >= 0) {
                            s.append(new String(buffer, 0, read));
                        }
                    }
                } // TODO: close streams?

                // Create XML Document from the ProxyEndpoint XML
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                document = docBuilder.parse(new InputSource(new StringReader(s.toString())));
                
            } else {
                System.out.println("GET request failed");
            }
        } catch (Exception e)  {
            e.printStackTrace();
        }
        return document;
    }

    private ArrayList<String> parseXML(Document document) 
    {
        ArrayList<String> s = new ArrayList<String>();
        try {
            // look for virtual hosts elements, there can be multiple
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodelist = (NodeList) xPath.evaluate("/ProxyEndpoint/HTTPProxyConnection/VirtualHost/text()", document, XPathConstants.NODESET);
            
            for (int i = 0; i < nodelist.getLength(); ++i) {
                Node node = nodelist.item(i);
                s.add(node.getNodeValue());                
            }


        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public ArrayList<APIProxy> process() 
    {
        // get all api proxies deployed
        ArrayList<APIProxy> apis = parseAPIs(getAPIProxyDeployments());

        for(APIProxy api: apis) {
            // get zip and find virtual host
            ArrayList<String> vhosts = parseXML(getAPIProxyXML(api));
            api.setVirtualHost(vhosts);
        }
        return apis;
    }
}