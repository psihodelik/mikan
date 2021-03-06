package db

import java.util.UUID

import com.google.inject.ImplementedBy
import models.Account
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future


@ImplementedBy(classOf[MongoAccountDao])
trait AccountDao {
  def save(user: Account): Future[WriteResult]

  def find(accId: UUID): Future[Option[Account]]

  def update(acc: Account): Future[WriteResult]

  def clearAccounts(): Future[WriteResult]
}
