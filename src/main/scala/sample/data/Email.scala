package sample.data

import io.circe.{Decoder, Encoder}
import zio.{IO, UIO}

import scala.util.matching.Regex
import sample.data.Error.EmailNotValid

final case class Email(value: String) extends AnyVal

object Email {

  private val pattern = new Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\\b")

  /**
    * Smart constructor: Normally you would validate an email before allowing it to be constructed in the first place.
    */
  def validate(str: String): IO[EmailNotValid, Email] =
    if (pattern.matches(str)) UIO.succeed(Email(str))
    else IO.fail(EmailNotValid(str))

  implicit val encoder: Encoder[Email] = Encoder.encodeString.contramap(_.value)
  implicit val decoder: Decoder[Email] = Decoder.decodeString.map(apply)

  val Test = Email("test@test.com")
}
