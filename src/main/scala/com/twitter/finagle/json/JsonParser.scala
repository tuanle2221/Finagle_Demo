package com.twitter.finagle.json

class JsonParser {
  //var str = """"ip":"127.0.0.1","port":8080,"coin_type":"USD","value":1000,"trans":1"""
  
  val tweetRegex = "\"ip\":\"(.*)\",\"port\":(.*),\"coin_type\":\"(.*)\",\"value\":(.*),\"trans\":(.*)".r
  def parse(str:String,result:Array[String]) {
    var result: Array[String] = new Array[String](5)
    tweetRegex.findFirstMatchIn(str) match{
      case Some(m) => {
        result(0) = str.substring(m.start(1),m.end(1))
        result(1) = str.substring(m.start(2),m.end(2))
        result(2) = str.substring(m.start(3),m.end(3))
        result(3) = str.substring(m.start(4),m.end(4))
      }
      case _ => None
    }
  }
}