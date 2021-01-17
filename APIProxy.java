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