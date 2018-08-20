package example.akka.remote.server;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.remote.server.enums.MemberConnectionStatus;
import example.akka.remote.server.enums.MessageType;
import java.util.HashMap;
import java.util.Map;

public class Actor2 extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    Cluster cluster = Cluster.get(getContext().getSystem());
    private final ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    Map members = new HashMap();

    @Override
    public void preStart() {
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(),
                ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class,ClusterEvent.MemberRemoved.class,ClusterEvent.MemberUp.class);
        mediator.tell(new DistributedPubSubMediator.Subscribe(MessageType.CHANNEL_ACTOR_NAMES.value(),getSelf()),getSelf());
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
        mediator.tell(new DistributedPubSubMediator.Unsubscribe(MessageType.CHANNEL_ACTOR_NAMES.value(),getSelf()),getSelf());
    }

    @Override
    public Receive createReceive() {

        AkkaMessages actorNameMessage = new AkkaMessages(MessageType.SEND_ACTOR_NAMES.value());
        AkkaMessages dataListName = new AkkaMessages(MessageType.SEND_DATA_LIST_NAMES.value());
        AkkaMessages channelName = new AkkaMessages(MessageType.SEND_CHANNEL_NAMES.value());

        return receiveBuilder()
                .matchEquals(actorNameMessage, mUp->{
                    if(!getSender().equals(getSelf())) {
                        AkkaMember akkaMember = new AkkaMember(getSender(),MemberConnectionStatus.NOT_KNOWN.status(),mUp.getMessage());
                        if(!members.containsKey(getSender().path().parent()))
                            members.put(getSender().path().parent(),akkaMember);

                        AkkaMessages akkaMessages = new AkkaMessages(null,MessageType.SEND_CONNECTION_REQUEST.value());
                        // ActorSelection selection = getContext().actorSelection(getSender().path());
                        // selection.tell(akkaMessages,getSelf());
                        getSender().tell(akkaMessages , getSelf());
                    }
                })
                .matchEquals(dataListName , mUp->{
                    log.info("Actor2 datalistname"+mUp.getMessage().toString());
                    Map subscribedData = new HashMap();
                    // Sadece 2.indistekine subscribe olalım diye yaptık.
                    mUp.getMessage().forEach((key, value) -> {
                        if(key.equals("2")){
                            subscribedData.put(key,value);
                        }
                    });
                    AkkaMessages akkaMessages = new AkkaMessages(subscribedData,MessageType.SEND_SUBSCRIBE_LIST.value());
                    getSender().tell(akkaMessages,getSelf());
                })
                .matchEquals(channelName,mUp->{
                    mUp.getMessage().forEach((key, value) -> {
                        log.info("actor2 "+value+"subscrie oldu");
                        mediator.tell(new DistributedPubSubMediator.Subscribe(value.toString(),getSelf()),getSelf());
                    });
                })
                .match(DoobData.class,mUp->{
                    log.info("selam"+mUp.getData()+"- "+mUp.getOigId()+"-"+mUp.getChannelName());
                })
                .match(ClusterEvent.MemberUp.class, mUp -> {
                    log.info("Member is Up: {}", mUp.member());
                    // İlk up olan member kendisi. Sonrasında kendi actor bilgisini herkese publish ediyor.
                    if(mUp.member().upNumber()==1) {
                        Thread.sleep(1500);
                        String actorName = this.getClass().getSimpleName();
                        Map actorNames = new HashMap();
                        actorNames.put(0,actorName);
                        AkkaMessages sendActorNames = new AkkaMessages(actorNames,MessageType.SEND_ACTOR_NAMES.value());
                        mediator.tell(new DistributedPubSubMediator.Publish(MessageType.CHANNEL_ACTOR_NAMES.value(),sendActorNames),getSelf());
                    }
                })
                .match(ClusterEvent.UnreachableMember.class, mUnreachable -> {
                    log.info("Member detected as unreachable: {}", mUnreachable.member());
                })
                .match(ClusterEvent.MemberRemoved.class, mRemoved -> {
                    log.info("Member is Removed: {}", mRemoved.member());
                })
                .match(ClusterEvent.MemberEvent.class, message -> {
                    // ignore
                })
                .match(DistributedPubSubMediator.SubscribeAck.class,mUp -> {
                    log.info("Subscribed to "+mUp.toString());
                })
                .build();
    }

}
