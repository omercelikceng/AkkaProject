package example.akka.remote.server;

import akka.actor.Actor;
import akka.actor.ActorRef;
import java.util.List;
import java.util.Map;

public class AkkaMember {
    private ActorRef actor;
    private String memberConnectionStatus;
    private Map actorNames;

    public AkkaMember() {
    }

    public AkkaMember(ActorRef actor, String status, Map actorNames) {
        this.actor = actor;
        this.memberConnectionStatus = status;
        this.actorNames = actorNames;
    }

    public ActorRef getActor() {
        return actor;
    }

    public void setActor(ActorRef actor) {
        this.actor = actor;
    }

    public String getStatus() {
        return memberConnectionStatus;
    }

    public void setStatus(String status) {
        this.memberConnectionStatus = status;
    }

    public Map getActorNames() {
        return actorNames;
    }

    public void setActorNames(Map actorNames) {
        this.actorNames = actorNames;
    }
}
