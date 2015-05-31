package controllers

import scala.sys.process.{Process, ProcessLogger}
import java.io.File
import play.api._
import play.api.mvc._
import models.{TuneMetadata, TuneRef}
import play.api.data._
import play.api.data.Forms._
import Forms._
import utils._
import scala.util.{Success, Failure, Try}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.util.Random
import dispatch.Defaults._

object Application extends Controller {

  val version = "0.8.0 Beta" 
  val random = new Random(System.currentTimeMillis())

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def upload = Action { 
    Ok(views.html.uploadtune("unused message")) 
  }  

  def processUpload = Action(parse.multipartFormData) { request =>
    request.body.file("midi").map { midi =>
      val filename = midi.filename 
      // generate a new file prefix with a random element to prevent files from multiple users clashing when written to the file system
      val slug = "_" + random.alphanumeric.take(5).mkString
      val fileprefix = filename.takeWhile(c => c != '.').filter(_.isLetterOrDigit) + slug
      val newfilename = fileprefix + ".mid"
      val contentType = midi.contentType
      Logger.debug(s"file length: ${midi.ref.file.length} content type: $contentType")
      // do basic validation on the submission
      if ((Some("audio/midi") != contentType) && (Some("audio/mid") != contentType))  {      
         val badContentType = contentType.getOrElse("")
         Redirect(routes.Application.error(s"Not a midi file: $badContentType"))
      }
      else if (40000 < midi.ref.file.length()) {      
         Redirect(routes.Application.error(s"File too big (${midi.ref.file.length()})"))
      }    
      else { 
         Logger.debug(s"file for upload: $filename")
         midi.ref.moveTo(new File(Utils.midiDir + "//"  + newfilename))
         Redirect(routes.Application.convert(fileprefix, None, None, None)).withSession("slug" -> slug)
      }
    }.getOrElse {
      Redirect(routes.Application.error("No file supplied"))
    }
  }

  def convert(filename: String, abc: Option[String], tuneImageRef: Option[String], tuneImageError: Option[String]) = Action { 
    Ok(views.html.convert(metadataForm, filename) (abc) (tuneImageRef) (tuneImageError)) 
  }  

  def processMetadata = Action { implicit request => {     
    val slug = request.session.get("slug").getOrElse("")
    metadataForm.bindFromRequest.fold (
      errors => {
         Logger.debug("metadata form errors: " + errors)
         BadRequest(views.html.convert(errors, "")(None)(None)(None))
      },
      label => {
        val metadata = metadataForm.bindFromRequest.get
        transcodeMidiToAbc(metadata) match {
          case (Left(msg)) =>  Redirect(routes.Application.error(s"error: $msg"))
          case (Right(fn)) =>  {

                Logger.debug(s"metadata rhythm: ${metadata.rhythm}")
                val source = scala.io.Source.fromFile(fn)
                val lines = try source.mkString finally source.close()
                val abc = Utils.excise(slug, lines)
                val resultDisjunctionFuture = Proxy.postTune(Utils.genre(metadata.rhythm), lines)
               
                try {
                  val result: Try[Either[String, TuneRef]] = Await.ready(resultDisjunctionFuture, Duration(1200, MILLISECONDS)).value.get
                  result match {

                    case Success(either) => either match {
                      case (Left(error)) =>  Ok(views.html.convert(metadataForm.bindFromRequest, metadata.filename) (Some(abc)) (None) (Some(error))   )   
                      case (Right(tuneRef)) => {
                         val url = Utils.imageUrl(tuneRef)
                         Ok(views.html.convert(metadataForm.bindFromRequest, metadata.filename) (Some(abc)) (Some(url)) (None)  )   
                         }
                      }

                    case Failure(e) => {
                       Ok(views.html.convert(metadataForm.bindFromRequest, metadata.filename) (Some(abc)) (None) (Some(e.getMessage())))  
                    }

                  }
                }
                catch {
                   case (e:Exception) => { 
                     Logger.debug(s"no score transcoding service: ${e.getMessage()}")
                     Ok(views.html.convert(metadataForm.bindFromRequest, metadata.filename) (Some(abc)) (None) (Some("service providing scores not available")))  
                     }
                }     
                  
            }    
        }
       
      }
    )
 
    }
  }

 
  def about = Action { 
    Ok(views.html.about(version))
  }   

  def credits = Action { 
    Ok(views.html.credits("home"))
  }   

  def contact = Action { 
    Ok(views.html.contact("home"))
  }   

  def help = Action { 
    Ok(views.html.help("home"))
  }  

  def error(message: String) = Action { 
     Ok(views.html.error(message))
   }

  private def transcodeMidiToAbc (metadata: TuneMetadata): Either[String, File] = {
    import scala.collection.mutable.StringBuilder    

    val out = new StringBuilder
    val err = new StringBuilder

    val logger = ProcessLogger(
      (o: String) => out.append(o),
      (e: String) => err.append(e))
      
    val script = Utils.scriptDir + "/" +  "midi2abc.sh" 
    val cmd = script + " " + Utils.midiDir + " " + Utils.abcDir + " " + metadata.filename + " " + metadata.trackno +
                       " " + metadata.leadin + " " +  metadata.notelen +  " " + metadata.rhythm + " " + metadata.key + " " + metadata.mode

    Logger.debug(cmd)

    val pb = Process(cmd)
    val exitValue = pb.run(logger).exitValue

    exitValue match {
      case 0 => { 
                 val fileName = Utils.abcDir + "//"  + metadata.filename + ".abc"
                 val file = new File(fileName)
                 Right(file)
                 }
      case _ => { 
                 Logger.debug("error " + err.toString)
                 Left(err.toString)
                }
    } 
  }

}
