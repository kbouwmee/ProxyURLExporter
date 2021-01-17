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

public class APIProxy {
    public String name;
    public String deployedRevision;
    public ArrayList<String> virtualHosts;
    public String basePath;

    public APIProxy(String name, String r, String bp) {
        this.name = name;
        this.deployedRevision = r;
        this.basePath = bp;
    }

    public void pushVirtualHost(String v) {
        this.virtualHosts.add(v);
    }

    public void setVirtualHost(ArrayList<String> v) {
        this.virtualHosts = v;
    }

    public boolean containsVirtualHost(String vhost) {
        for(String vhostloop : virtualHosts) {
            if(vhostloop.equals(vhost)) {
                return true;
            }
        }
        return false;
    }
}