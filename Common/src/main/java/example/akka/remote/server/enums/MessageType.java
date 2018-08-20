package example.akka.remote.server.enums;

public enum MessageType {
    CHANNEL_ACTOR_NAMES("CHANNELACTORNAMES"),
    SEND_ACTOR_NAMES("ACTORNAMES"),
    SEND_SUBSCRIBE_LIST("SUBSCRIBELIST"),
    SEND_CONNECTION_REQUEST("CONNECTIONREQUEST"),
    SEND_DATA_LIST_NAMES("DATALISTNAMES"),
    SEND_CHANNEL_NAMES("SENDCHANNELNAME");

    private String value;

    MessageType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
