package main.scala

import akka.actor._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.concurrent.duration._


case object GenerateTwitter
case object GetTwitter


class Client(server: ActorSelection, followerNum:Int, twitterRequestedNum:Int,
    postTwitterInterval:FiniteDuration, flushTwitterInterval:FiniteDuration) extends Actor {
  var userId:Int = -1;
  val randomGenerator:Random = new Random

  server ! Register 
  var postTwitterTimer:Cancellable = null
  var flushTwitterTimer:Cancellable = null
//  var postTwitterInterval = 5.
//  var flushTwitterInterval = 10 seconds

  def simulateBegin = {   
    postTwitterRepeatly
    flushTwitterRepeatly
  }
  
  def postTwitterRepeatly = {
    val system = context.system
    import system.dispatcher
    val timeToStart = randomGenerator.nextInt(1000)
    postTwitterTimer = context.system.scheduler.schedule(timeToStart.milliseconds, postTwitterInterval, self, GenerateTwitter)
  }
  
  def flushTwitterRepeatly = {
    val system = context.system
    import system.dispatcher
    val timeToStart = randomGenerator.nextInt(1000)
    flushTwitterTimer = context.system.scheduler.schedule(timeToStart.milliseconds, flushTwitterInterval, self, GetTwitter)
  }
  
  def receive={
    case ShutDown=>
      println("server is down, twitter done")
      context stop self
    case s:String=>
//      println("from server: " + s)
    case AssignUserId(id, totalUserNum)=>
      userId = id
      val follower = generateFollower(totalUserNum)
      server ! FollowerList(userId, follower)
      simulateBegin
    case GenerateTwitter=>
      val content:String = "this is a twitter from " + userId + "!"
//      println(userId + "have sent a twitter")
      server ! SendTwitter(userId, content)
    case GetTwitter=>
      server ! TwitterRequest(twitterRequestedNum, userId)
    case TwitterResponse(twitters:ArrayBuffer[String])=>
//      receiveTwitters(twitters)
  }
 
  def receiveTwitters(twitters:ArrayBuffer[String]) = {
    println(userId + "'s home line")
    if( twitters.isEmpty )
      println("no new twitter available")
    else
      println("new twitters:")
    for(t <- twitters)
      println(t)
  }
  def generateFollower(totalUserNum:Int): ArrayBuffer[Int]= {
    var follower = ArrayBuffer[Int]()
    for(i <- 1 to followerNum){
      follower += randomGenerator.nextInt(totalUserNum)
    }
//    println("i am client" + userId + "---my follower is " + follower)
    return follower
  }
}