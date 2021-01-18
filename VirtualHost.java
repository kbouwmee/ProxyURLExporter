// Copyright 2018-2020 Google LLC
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

import java.util.ArrayList;

public class VirtualHost {
    public String name;
    public ArrayList<String> hostAliases;
    public int port;
    public boolean isSSLenabled;
    
    public String basePath;

    public VirtualHost(String name, ArrayList<String> hostAliases, int port, boolean isSSL) {
        this.name = name;
        this.hostAliases = hostAliases;
        this.port = port;
        this.isSSLenabled = isSSL;
    }

    public String getDeploymentURLs(APIProxy api) {
        StringBuilder r = new StringBuilder();
        
        for(String ha : hostAliases) {
            if(isSSLenabled) r.append(" https://");
            else r.append("https://");
            r.append(ha).append(api.basePath);
        }
        return r.toString();
    }
}