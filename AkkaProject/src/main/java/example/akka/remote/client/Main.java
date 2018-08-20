package example.akka.remote.client;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

public class Main {
    public static void main(String[] args) {

        ActorSystem system = ActorSystem.create("AkkaRemoteServer", ConfigFactory.load());
        system.actorOf(Props.create(Actor1.class),"Actor1");
    }
}
