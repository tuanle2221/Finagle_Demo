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


object Client {
  def main(args: Array[String]) {
    // Construct a client, port 1000
    
    print("\nIp address: "); var ip = readLine	// Input IP address
    print("Value     : "); var valu = readInt	// Value of coin
    print("Transaction type(1 to send, 2 to receive): ");var trans = readInt // choose type of transaction
    
    // JSON format
    var str = """"ip":"""" + ip + """","value":""" + valu + ""","trans":""" + trans
    
	val client: Service[String, String] = ClientBuilder()
	.codec(StringCodec)
	.hosts(new InetSocketAddress(ip, 1000))
	.hostConnectionLimit(1)
	.build()

    // Issue a newline-delimited request, respond to the result
    // asynchronously:
    //println()
	
	// client send reruest and receive response from server
	
	client(str+"\n") onSuccess { result =>
	  // Show JSON string
		println("\nResponse with JSON format: " + result)
		println("\nJSON string after parsed and printed in screen:\n")
		
		// Parse JSON string into variable
		var results:Array[String] = new Array[String](3)
        val tweetRegex = "\"say\":\"(.*)\",\"number\":(.*),\"account\":(.*)".r
        tweetRegex.findFirstMatchIn(result) match{
		  case Some(m) => {
		    results(0) = result.substring(m.start(1),m.end(1))
	        results(1) = result.substring(m.start(2),m.end(2))
	        results(2) = result.substring(m.start(3),m.end(3))
		  }
		  case _ => None
        }
		
		println("Server said : " + results(0))
		println("Number      : " + results(1))
		println("Your account: " + results(2) + "\n")
		
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
