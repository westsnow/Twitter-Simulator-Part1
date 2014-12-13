package main.scala

import akka.actor.ActorRef
import scala.collection.mutable.ArrayBuffer


//client to server
case object Register
case class FollowerList(userId:Int, friendList: ArrayBuffer[Int])
case class SendTwitter(userId:Int, content:String)
case class TwitterRequest(requestNum:Int, userId:Int)

//server to client
case object ShutDown
case class AssignUserId(userId:Int, totalUserNum:Int)
case class TwitterResponse(twitters:ArrayBuffer[String])