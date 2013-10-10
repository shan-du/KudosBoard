package models.domain

import scala.slick.driver.MySQLDriver.simple._
import play.api.libs.json.Json
import org.joda.time.DateTime
import mappers.DateTimeMapper._

case class Card( id: Option[Int],
                 sender_id: String,
                 date: DateTime,
                 message: String)

object Card {
  implicit val format = Json.format[Card]
}

object Cards extends Table[Card]("card") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def sender_id = column[String]("sender")
  def date = column[DateTime]("date")
  def message = column[String]("message")

  def * = id.? ~ sender_id ~ date ~ message <> (Card(_,_,_,_), Card.unapply)
  def autoInc = id.? ~ sender_id ~ date ~ message <> (Card(_,_,_,_), Card.unapply) returning id

  def sender = foreignKey("card_sender_FK", sender_id, Users)(_.username)

  def comments = for (comment <- Comments if comment.card_id === id) yield comment
  def recipients = for {
    recipient <- Recipients if recipient.card_id === id
    user <- recipient.user
  } yield user
  def coAuthors = for {
    coAuthor <- CoAuthors if coAuthor.card_id === id
    user <- coAuthor.user
  } yield user
}
