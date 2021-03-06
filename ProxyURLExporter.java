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

import java.io.InputStream;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ProxyURLExporter {

    public ProxyURLExporter() {

    }

    private static Properties readProperties(String configFile, String t) {
        Properties prop = new Properties();
        InputStream input = null;
    
        try {
    
            input = new FileInputStream(configFile);
    
            // load a properties file
            prop.load(input);
    
            // set apigee properties
            prop.setProperty("apigee.org", prop.getProperty(t+".org"));
            prop.setProperty("apigee.env", prop.getProperty(t+".env"));
            prop.setProperty("apigee.vhost", prop.getProperty(t+".vhost"));
            prop.setProperty("apigee.token", prop.getProperty(t+".token"));

    
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }

    public static void main(String[] args) 
    {
        System.out.println(" ");
        System.out.println("This program will get all API Proxies deployed on");
        System.out.println("a virtual host in an environment for specific org.");
        System.out.println(" ");

        if(args.length != 1) {
            System.out.println("ERROR: Use following parameters:");       
            System.out.println("- config file");
        } else {
            String configFile = args[0];
        
            Properties p = readProperties(configFile, "apigee");

            String org = p.getProperty("apigee.org");
            String env = p.getProperty("apigee.env");
            String vhost = p.getProperty("apigee.vhost");
            String token = p.getProperty("apigee.token");

            System.out.println("=============== Input ================");
            System.out.println("org        : " + org);
            System.out.println("env        : " + env);
            System.out.println("vhost      : " + vhost);
            System.out.println("======================================");

            // Get hostalias from the vhost so that we can build complete url
            VirtualHostDownloader vhDowner = new VirtualHostDownloader(org, env, vhost, token);
            VirtualHost vh = vhDowner.process();
            if(vh == null) System.out.println("Virtual host was not found");

            // Create downloader object
            APIProxyDownloader downloader = new APIProxyDownloader(org, env, token);
            
            // Get all API Proxies deployed in the org
            //  and download details of all these API Proxies
            ArrayList<APIProxy> apis = downloader.process();
            
            // output all API Proxies that match virtual host
            if(apis.size() > 0) {
                System.out.println("The following API proxies are deployed on virtual host: " + vhost);
                for(APIProxy api: apis) {
                    if(api.containsVirtualHost(vhost)) {
                        String url = vh.getDeploymentURLs(api);
                        System.out.println(api.name + "  | revision: " + api.deployedRevision + "  | " + url );
                    }
                }
            } else {
                System.out.println("No APIs found. Does the environment you provided exists in the org? Or see error message above.");
            }
        }
    }
}