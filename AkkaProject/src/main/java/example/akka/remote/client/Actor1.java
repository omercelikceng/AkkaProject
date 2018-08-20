package example.akka.remote.client;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.remote.server.*;
import example.akka.remote.server.enums.MemberConnectionStatus;
import example.akka.remote.server.enums.MessageType;
import java.util.*;

public class Actor1 extends AbstractActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    Cluster cluster = Cluster.get(getContext().getSystem());
    private final ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();

    Map members = new HashMap();
    Map data = new HashMap();

    @Override
    public void preStart() {
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(),
                ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class,ClusterEvent.MemberRemoved.class);
        mediator.tell(new DistributedPubSubMediator.Subscribe(MessageType.CHANNEL_ACTOR_NAMES.value(),getSelf()),getSelf());
        createDummyData();
    }

    //re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
        mediator.tell(new DistributedPubSubMediator.Unsubscribe(MessageType.CHANNEL_ACTOR_NAMES.value(),getSelf()),getSelf());
    }

    private void createDummyData(){
        DoobData doobData = new DoobData("111","Data 1","Channel 1");
        DoobData doobData2 = new DoobData("222","Data 2" , "Channel 2");
        DoobData doobData3 = new DoobData("333","Data 3","Channel 3");

        data.put("1",doobData);
        data.put("2",doobData2);
        data.put("3",doobData3);
    }

    @Override
    public Receive createReceive() {

        AkkaMessages actorNameMessage = new AkkaMessages(MessageType.SEND_ACTOR_NAMES.value());
        AkkaMessages connectionRequest = new AkkaMessages(MessageType.SEND_CONNECTION_REQUEST.value());
        AkkaMessages subscribeList = new AkkaMessages(MessageType.SEND_SUBSCRIBE_LIST.value());


        return receiveBuilder()
                .matchEquals(actorNameMessage, mUp->{
                    log.info("Actor1 "+mUp.getMessage().toString()+" actorname al");
                    if(!getSender().equals(getSelf()) && !members.containsKey(getSender().path().parent())) {
                        AkkaMember akkaMember = new AkkaMember(getSender(),MemberConnectionStatus.NOT_KNOWN.status(),mUp.getMessage());
                        members.put(getSender().path().parent(),akkaMember);
                    }
                })
                .matchEquals(connectionRequest,mUp ->{
                    log.info("Actor 1 e connection isteği geldi.");
                    boolean randomRequestAccept = true;
                    String connectionStatus = randomRequestAccept ? MemberConnectionStatus.CONNECTION_ACCEPT.status() :
                            MemberConnectionStatus.CONNECTION_REJECT.status();
                    if(members.containsKey(getSender().path().parent())){
                        AkkaMember akkaMember = (AkkaMember) members.get(getSender().path().parent());
                        akkaMember.setStatus(connectionStatus);
                    }
                    else {
                        AkkaMember akkaMember = new AkkaMember(getSender(),connectionStatus,mUp.getMessage());
                        members.put(getSender().path().parent(),akkaMember);
                    }
                    log.info(getSender().path().parent()+" bilgisi de burda");
                    if(randomRequestAccept){
                        Map dataList = new HashMap();
                        data.forEach((key, value) -> {
                            DoobData doobData = (DoobData) value;
                            dataList.put(key,doobData.getOigId());
                            log.info("actor 1 datalar"+doobData.getOigId().toString());
                        });
                        log.info("actor1 connection request bitti");
                        AkkaMessages akkaMessages = new AkkaMessages(dataList,MessageType.SEND_DATA_LIST_NAMES.value());
                        getSender().tell(akkaMessages,getSelf());
                    }
                })
                .matchEquals(subscribeList , mUp ->{
                    data.forEach((key, value) -> {
                        DoobData doobData = (DoobData) value;
                        if(mUp.getMessage().containsValue(doobData.getOigId())){
                            String channelName = doobData.getChannelName();
                            Map dataList = new HashMap();
                            dataList.put(doobData.getOigId(),channelName);
                            log.info("actor 1 subscribe"+doobData.getOigId().toString()+" -- "+channelName);
                            AkkaMessages akkaMessages = new AkkaMessages(dataList,MessageType.SEND_CHANNEL_NAMES.value());
                            getSender().tell(akkaMessages,getSelf());
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mediator.tell(new DistributedPubSubMediator.Publish(channelName,doobData),getSelf());

                        }
                    });
                })
                .match(ClusterEvent.MemberUp.class, mUp -> {
                    log.info("Member is Up: {}", mUp.member());
                    String actorName="";
                    if(mUp.member().upNumber()==1) {
                        Thread.sleep(1500);
                        actorName = this.getClass().getSimpleName();
                        log.info("Actor1 "+actorName+"girdi");
                        Map actorNames = new HashMap();
                        actorNames.put(0,actorName);
                        AkkaMessages akkaMessages = new AkkaMessages(actorNames,MessageType.SEND_ACTOR_NAMES.value());
                        mediator.tell(new DistributedPubSubMediator.Publish(MessageType.CHANNEL_ACTOR_NAMES.value(),akkaMessages),getSelf());
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
