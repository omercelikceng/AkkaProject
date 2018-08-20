package example.akka.remote.server.enums;

public enum MemberConnectionStatus {
    NOT_KNOWN("NOT KNOWN"),
    CONNECTION_REJECT("CONNECTION REJECTED"),
    CONNECTION_ACCEPT("CONNECTION ACCEPT"),
    BAN("BAN");

    private String status;

    MemberConnectionStatus(String status) {
        this.status = status;
    }

    public String status() {
        return status;
    }
}
