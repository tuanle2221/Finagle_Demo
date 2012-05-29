package com.twitter.finagle.opencoin.demo

import java.net.InetSocketAddress
import com.twitter.finagle.Codec
import com.twitter.finagle.CodecFactory
import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.builder.Server
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.util.Future
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder
import org.jboss.netty.handler.codec.frame.Delimiters
import org.jboss.netty.handler.codec.string.StringDecoder
import org.jboss.netty.handler.codec.string.StringEncoder
import org.jboss.netty.util.CharsetUtil
import java.net.NetworkInterface
import scala.collection.JavaConversions._
import java.lang.Exception


object isNumber {
	def isNumeric(input: String): Boolean = input.forall(_.isDigit)
}

object Server {
  def main(args: Array[String]) {
    /**
     * A very simple service that simply echos its request back
     * as a response. Note that it returns a Future, since everything
     * in Finagle is asynchronous.

     */
    var i=0
    var j=0
    var ip = new Array[String](10)
	  NetworkInterface.getNetworkInterfaces.foreach{it =>it.getInetAddresses.foreach{inet=>inet.getAddress()
	  	if(i%2!=0)
	  	{
	  		ip(j) = inet.toString().substring(1,inet.toString().length())
	  		j=j+1
	  	}
	  	i = i + 1
      }
    }
    var ip_address = ip(0)
    println("Server IP address: " + ip(0))
    var account = 1000000
    val service = new Service[String, String] {
      def apply(request: String) = {
    	  var str = request
    	  
    	  if(str.substring(0,1)=="G"){
    	    str = str.substring(5) 
    	    str = str.substring(0,str.length()-9)
    	  }
    	  println(str)
    	  
	  println("Client sent: " + str)
    	  //var p = new JsonParser()
    	  
    	  var result:Array[String] = new Array[String](3)
        
          val tweetRegex = "\"ip\":\"(.*)\",\"value\":(.*),\"trans\":(.*)".r
          tweetRegex.findFirstMatchIn(str) match{
        	  case Some(m) => {
	        	  result(0) = str.substring(m.start(1),m.end(1))
	        	  result(1) = str.substring(m.start(2),m.end(2))
	        	  result(2) = str.substring(m.start(3),m.end(3))
        	  }
        	  case _ => None
          }
    	  println()
    	  
		 var t = if(result(2)=="1") "send" else "receive"
		 println("Client's transfer: " + t)
		 println("Value: " + result(1))
		 print("What do you say with client? ")
		 var say = readLine
		 var flag = false
		 var number = ""
		 while(!flag)
		 {
			 print("Input number: ")
			 number = readLine
			 if(isNumber.isNumeric(number))
				 flag = true
			 else
			     flag = false
		 }
		 println("Client want to " + t + " " + result(1))
		 if(result(2)=="1")
		 {
			 account = account - result(1).toInt
			 var str_json = """"say":"""" + say + """","number":""" + number + ""","account":""" + account
			 Future.value(str_json)
		 }else
		 {
			 account = account + result(1).toInt
			 var str_json = """"say":"""" + say + """","number":""" + number + ""","account":""" + account
			 Future.value(str_json)
		 }
	  }
    }

    // Bind the service to port 8080
    val server: Server = ServerBuilder()
      .codec(StringCodec)
      .bindTo(new InetSocketAddress("127.0.0.1",8080))
      .name("echoserver")
      .build(service)
  }
}



/**
 * A really simple demonstration of a custom Codec. This Codec is a newline (\n)
 * delimited line-based protocol. Here we re-use existing encoders/decoders as
 * provided by Netty.
 */
object StringCodec extends StringCodec

class StringCodec extends CodecFactory[String, String] {
  def server = Function.const {
    new Codec[String, String] {
      def pipelineFactory = new ChannelPipelineFactory {
        def getPipeline = {
          val pipeline = Channels.pipeline()
          pipeline.addLast("line",
            new DelimiterBasedFrameDecoder(100, Delimiters.lineDelimiter: _*))
          pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8))
          pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8))
          pipeline
        }
      }
    }
  }

  def client = Function.const {
    new Codec[String, String] {
      def pipelineFactory = new ChannelPipelineFactory {
        def getPipeline = {
          val pipeline = Channels.pipeline()
          pipeline.addLast("stringEncode", new StringEncoder(CharsetUtil.UTF_8))
          pipeline.addLast("stringDecode", new StringDecoder(CharsetUtil.UTF_8))
          pipeline
        }
      }
    }
  }
}
