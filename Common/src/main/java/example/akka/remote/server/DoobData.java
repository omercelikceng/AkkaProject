package example.akka.remote.server;

import java.io.Serializable;

public class DoobData implements Serializable {
    private String oigId;
    private String data;
    private String channelName;

    public DoobData(String oigId, String data) {
        this.oigId = oigId;
        this.data = data;
    }

    public DoobData(String oigId, String data, String channelName) {
        this.oigId = oigId;
        this.data = data;
        this.channelName = channelName;
    }

    public DoobData() {
    }

    public String getOigId() {
        return oigId;
    }

    public void setOigId(String oigId) {
        this.oigId = oigId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
