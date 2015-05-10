package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.data.validation._
import models.TuneMetadata

object Forms {
 
  val metadataForm = Form(
    mapping (
      "trackno"  -> text,
      "leadin"   -> text,
      "notelen"  -> text,
      "rhythm"   -> text,
      "key"      -> text,
      "mode"     -> text,
      "filename" -> text
    ) (TuneMetadata.apply)(TuneMetadata.unapply)
  )
 
 
}
