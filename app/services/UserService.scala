package services

import scala.slick.driver.MySQLDriver.simple._
import models.domain.{DAO, User}
import play.api.Play.current
import scala.util.Try
import Database.threadLocalSession
import play.api.db.DB

object UserService {

  val db = Database.forDataSource(DB.getDataSource())

  def ldapSync() {
    db.withSession {
      val usersInfo = LDAPContext.searchContext.findAll()
      usersInfo foreach { u =>

        val domainUser = for {
          user <- DAO.users
          if user.username === u.userName
        } yield user.firstname ~ user.lastname ~ user.email
        domainUser.firstOption match {
          case None =>
            val user = User(u.userName, u.email, u.firstName, u.lastName)
            Try(DAO.users.insert(user))
          case Some(_) =>
            domainUser.update((u.firstName, u.lastName, u.email))
        }
      }
    }
  }

  def getUser(username: String): Option[User] = {
    db.withSession {
      val findUser = for (user <- DAO.users if user.username === username) yield user
      findUser.firstOption
    }
  }

  def searchNames(firstNamePrefix: String, lastNamePrefix: String): List[User] = {
    db.withSession {
      val users = for {
        user <- DAO.users
        if user.firstname like s"$firstNamePrefix%"
        if user.lastname like s"$lastNamePrefix%"
      } yield user
      users.list
    }
  }

  def searchUsername(usernamePrefix: String): List[User] = {
    db.withSession {
      val query = for {
        user <- DAO.users
        if user.username startsWith usernamePrefix
      } yield user
      query.list
    }
  }

}
