package com.twitter.finagle.example.echo

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

object _Client {
  def main(args: Array[String]) {
    // Construct a client, and connect it to localhost:8080
   // println("\n")
    print("Ip address: "); var ip = readLine
    print("Port      : "); var port = readLine
    print("Coin type : "); var coin_type = readLine
    print("Value     : "); var valu = readLine
    print("Transaction type(1 to send, 2 to receive): ");var trans = readInt
    
    var str = """"ip":"""" + ip + """","port":""" +  port + ""","coin_type":"""" + coin_type + """","value":""" + valu + ""","trans":""" + trans
    println()
    
    //print(str)
    
	val client: Service[String, String] = ClientBuilder()
	.codec(StringCodec)
	.hosts(new InetSocketAddress(ip,port.toInt))
	.hostConnectionLimit(1)
	.build()

    // Issue a newline-delimited request, respond to the result
    // asynchronously:
    //println()
	client(str+"\n") onSuccess { result =>
		println()
		println("Result: " + result)
		println()
	} onFailure { error =>
		println("--------------------------")
		println("|....Connection failure....|")
		println("--------------------------")
		//error.printStackTrace()
	} ensure {
     // All done! Close TCP connection(s):
		client.release()
	}
  }
}
object _Server {
  def main(args: Array[String]) {
    /**
     * A very simple service that simply echos its request back
     * as a response. Note that it returns a Future, since everything
     * in Finagle is asynchronous.
     */
    //var result:Array[String] = new Array[String](5)
    //val tweetRegex = "\"ip\":\"(.*)\",\"port\":(.*),coin_type\":\"(.*)\",\"value\":(.*)".r
    val service = new Service[String, String] {
      def apply(request: String)= 
      {
        
        //var Inet:InetAdress = InetAd
        
        var str = request
        //var p = new JsonParser()
        
        var result:Array[String] = new Array[String](6)
        
        val tweetRegex = "\"ip\":\"(.*)\",\"port\":(.*),\"coin_type\":\"(.*)\",\"value\":(.*),\"trans\":(.*)".r
        tweetRegex.findFirstMatchIn(str) match{
        	case Some(m) => {
        	  result(0) = str.substring(m.start(1),m.end(1))
        	  result(1) = str.substring(m.start(2),m.end(2))
        	  result(2) = str.substring(m.start(3),m.end(3))
        	  result(3) = str.substring(m.start(4),m.end(4))
        	  result(4) = str.substring(m.start(5),m.end(5))
        	}
        	case _ => None
        }
        var t = if(result(4)=="1") "sended" else "received"
        println()
        println("Transaction: " + (if(result(4)=="1") "Send" else "Receive"))
        println("Coin type  : " + result(2))
        println("Values     : " + result(3))
        println("Transaction successfull")
		Future.value("Transaction successfull with " + result(3) + " " + result(2) + " are " + t )
      }
    }
    //var add = new InetSocketAddress()
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
