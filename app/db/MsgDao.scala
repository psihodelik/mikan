package db

import com.google.inject.ImplementedBy
import messages.InternalMsg
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

@ImplementedBy(classOf[MongoMsgDao])
trait MsgDao {
  def save(msg: InternalMsg): Future[WriteResult]

  def find(msg: String): Future[Option[InternalMsg]]

  def findAll(objTypeList: List[String], accId: String): Future[List[InternalMsg]]
}
