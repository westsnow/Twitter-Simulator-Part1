package main.scala


import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

object ClientMain {

  var actorpool = ArrayBuffer[ActorRef]();
  var clientNum = 100
  var followerNum = 10
  var twitterRequestedNum = 3
  var postTwitterInterval = 5 seconds
  var flushTwitterInterval = 10 seconds
  
  
  def main(args: Array[String]): Unit = {
   
    
    var remoteIP = "10.136.23.227"
    if(args.length < 6){
      println("Error:parameter not enough")
    }else{
        remoteIP = args(0)
        clientNum = args(1).toInt
        followerNum = args(2).toInt
        twitterRequestedNum = args(3).toInt
        postTwitterInterval = args(4).toInt.seconds
        flushTwitterInterval = args(5).toInt.seconds

    }
    val clientSystem = ActorSystem("client", ConfigFactory.load(ConfigFactory.parseString("""
   akka {
    actor {
      provider = "akka.remote.RemoteActorRefProvider"
    }
    log-dead-letters = off
  }
  """)))
  
    val remotePath = "akka.tcp://serverSys@" + remoteIP +":11111/user/server"
    println(remotePath)

    val remoteServer = clientSystem.actorSelection(remotePath)

    println(remoteServer.pathString)
    

    for (i <- 0 until clientNum) {
      val client = clientSystem.actorOf(Props(classOf[Client], remoteServer,followerNum, twitterRequestedNum,
          postTwitterInterval, flushTwitterInterval), name = "client" + i)
      actorpool += client;
    }

  }
}