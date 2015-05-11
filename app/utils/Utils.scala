package utils

import java.io.InputStream
import play.api.{Play, Logger}
import models.TuneRef
import javax.xml.bind.DatatypeConverter

object Utils {

  /** generate a css class of 'current' if this navigation link represents the current option */
  def cssClass(label: String, context: String): String = 
    if (label == context) "current" else ""

  /** convert an input stream to a byte array */
  def toByteArray(is: InputStream) : Array[Byte] = {
     Iterator continually is.read takeWhile (-1 !=) map (_.toByte) toArray
  }

  val musicrestUsername = Play.current.configuration.getString("musicrest.username").getOrElse("administrator")
  val musicrestPassword = Play.current.configuration.getString("musicrest.password").getOrElse("unknown")

  val dataHome = Play.current.configuration.getString("data.home").getOrElse("/var/data/midi2abc") 
 
  val scriptDir = dataHome + "/scripts"  
  val abcDir = dataHome + "/abc"  
  val midiDir = dataHome + "/midi"

  /** the remote server (musicrest) fro transcoding abc to images */
  def remoteService = {
     val remoteServer = Play.current.configuration.getString("musicrest.server").getOrElse("localhost:8080/musicrest/")
     "http://" + remoteServer  
   }

  /**  infer the genre from the rhythm */
  def genre(rhythm: String) : String = {
    if ( List ("Hornpipe","Jig","Polka","Reel","Slide","SlipJig") contains rhythm) {
      "irish"
    }
    else {
      "scandi"
    }
  }

  /** build a url of the tune image as served up by musicrest */
  def imageUrl(tuneRef: TuneRef) : String = {
     remoteService + "genre/" + tuneRef.genre + "/tune/" + tuneRef.name + "/temporary" +"/png"
  }

  def base64Encode(s: String) = DatatypeConverter.printBase64Binary( s.getBytes("UTF-8") )
     
  
}
