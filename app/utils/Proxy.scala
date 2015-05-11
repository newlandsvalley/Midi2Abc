package utils

import dispatch._, Defaults._
import play.api.mvc.{Request, Session, AnyContent}
import play.api.data.validation.{Constraint, Valid, Invalid, ValidationResult}
import play.api.{Play, Logger}
import java.io.{InputStream, InputStreamReader}
import javax.xml.bind.DatatypeConverter
import scala.util.{Try, Success, Failure}
import models.TuneRef


object Proxy {   

   val basicAuth = DatatypeConverter.printBase64Binary( (Utils.musicrestUsername + ":" + Utils.musicrestPassword).getBytes("UTF-8") )

   /** check that the musicrest service is up */
   def checkService(): Future[Either[String, String]] = withHttp { 
     http => {   
       val headers = Map("Accept" -> "text/plain; charset=UTF-8")   
       val urlString = Utils.remoteService
       val req = url(urlString) <:< headers
       val resp = Http(req OK as.String).either  
       for (exc <- resp.left) 
              yield "transcoding service not up: " +   exc.getMessage
     }
   }   

   /** post a new tune to musicrest 
    *
    * @param genre the genre of the tune
    * @param abc its ABC
    */
   def postTune[A](genre: String, abc: String ): Future[Either[String, TuneRef]] = withHttp {   
     http => {  
       val urlString =  Utils.remoteService + "genre/" + genre + "/transcode"

       Logger.debug("url:" + urlString)
       Logger.debug("abc:" + abc)
       
       /** construct the request headers for the proxy */
       val builder = Map.newBuilder[String, String]
       builder += "Accept" -> "text/plain"
       builder += "Content-Type" -> "application/x-www-form-urlencoded"
       builder += "Authorization" -> ("Basic " + basicAuth ) 
       val headers = builder.result
            
       def req =  (url(urlString) <:< headers).POST << Map("abc" -> abc)
     
       val resp: Future[Either[Throwable, String]] = Http(req OK as.String).either    
       val respl =  for (exc <- resp.left) 
              yield "Can't produce a score: " +   exc.getMessage
       for (n <- respl.right)
              yield TuneRef(genre, n)
     }
   }

   /** quick and dirty mechanism to detect connect exceptions */
   def isConnectException(e: Throwable) : Boolean = {
      if (e.isInstanceOf[java.util.concurrent.ExecutionException]) {
         val c = e.getCause()
         (c != null) && (c.isInstanceOf[java.net.ConnectException]) 
      }
      else
        false
   }
 
   private def withHttp [A] (body: (Http) => A) : A = {
      val http = new Http   
      try 
         body(http)
      finally {
        if (null != http) {
        try {
            http.shutdown
        }      	
        catch {
           case e: Exception  => None
        }
      }
    }
   }   
   
 
   

}
