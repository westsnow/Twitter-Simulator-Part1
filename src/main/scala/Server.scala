package main.scala

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.Queue
import scala.concurrent.duration._

case object ReportStatus

class Server(sec: Int, totalUserNum: Int) extends Actor {

  //    val start: Long = System.currentTimeMillis
  //    var clients = ArrayBuffer[ActorRef]()
  println("server init...")
  var idRange = 0
  var twitterRange = 0
  var twitterDeliverCounter = 0
  var twitterRecivedCounter = 0
  
  var messageQueues = HashMap[Int, Queue[Int]]()
  var followerTable = HashMap[Int, ArrayBuffer[Int]]()
  var twitterList = HashMap[Int, String]()
  var reportTimer: Cancellable = null
  val reportInterval = 1 seconds
  
  init
  
  println("server started." + self.path)
  println(totalUserNum + " users are expected");

  def reportRepeatly = {
    val system = context.system
    import system.dispatcher
    reportTimer = context.system.scheduler.schedule(1 seconds, reportInterval, self, ReportStatus)
  }

  def init = {
    for (i <- 0 to totalUserNum) {
      followerTable(i) = ArrayBuffer[Int]()
      messageQueues(i) = Queue[Int]()
    }
    reportRepeatly
  }
  def reportStatus = {
    println("------------------------------------------")
    println( idRange + "/" + totalUserNum + "has joined the network")
    println( twitterRange + "twitters has been posted by users")
    println( "server has sent " + twitterDeliverCounter + " twitters to users per second")
    println( "server has received " + twitterRecivedCounter + "twitters from users per second")
    twitterRecivedCounter = 0
    twitterDeliverCounter = 0
  }

  def setFollowerTable(userId: Int, friendList: ArrayBuffer[Int]) = {
    followerTable(userId) = friendList
  }

  def twitterEnqueue(userId: Int, content: String) = {
    //println(userId + " is twittering.......")
    twitterList(twitterRange) = content
    var followers: ArrayBuffer[Int] = followerTable(userId)
    for (follower <- followers) {
      //println("twitter from " + userId + " is added to queue of " + follower)
      var msgqueue = messageQueues(follower)
      msgqueue += twitterRange
      //        println(follower + "'s cur queue is " )
      //        for(n <- msgqueue)
      //          println(twitterList(n))
    }
    twitterRecivedCounter += 1
    twitterRange += 1
  }

  def twitterDequeue(num: Int, userId: Int): ArrayBuffer[String] = {
    var msgqueue = messageQueues(userId)
    val n = Math.min(msgqueue.size, num)
    var result = ArrayBuffer[String]()
    for (i <- 0 until n) {
      var tid = msgqueue.dequeue
      result += twitterList(tid)
    }
    twitterDeliverCounter += n
    return result
  }

  def receive = {
    case ReportStatus =>
      reportStatus
    case Register =>
      sender ! AssignUserId(idRange, totalUserNum);
      idRange += 1
    case TwitterRequest(num: Int, userId: Int) =>
      var twitters = twitterDequeue(num, userId)
      sender ! TwitterResponse(twitters)
    case FollowerList(userId: Int, friendList: ArrayBuffer[Int]) =>
      setFollowerTable(userId, friendList)
    case SendTwitter(userId: Int, content: String) =>
      twitterEnqueue(userId, content)
    case StopWork =>
      println("stop working")
      context stop self
      context.system.shutdown
  }
}