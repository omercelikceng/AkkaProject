package example.akka.remote.server;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by omercelik on 10.08.2018.
 */
public class AkkaMessages implements Serializable {

    private Map message;
    private String type;

    private static final long serialVersionUID = 1L;

    public AkkaMessages() {
    }

    public AkkaMessages(String type) {
        this.type = type;
    }

    public AkkaMessages(Map message, String type) {
        this.message = message;
        this.type = type;
    }

    public Map getMessage() {
        return message;
    }

    public void setMessage(Map message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AkkaMessages that = (AkkaMessages) o;

        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
