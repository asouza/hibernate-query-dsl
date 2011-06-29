package br.com.caelum.hibernatequerydsl

import conditions.Cond
import scala.collection.JavaConversions._
import org.hibernate.{Session, Query}
class PimpedQuery(query: Query) {
  def withParams(params: (String, Any)*) = {
    params.foreach((param) => {
      query.setParameter(param._1, param._2)
    })
    query
  }

  def unique[T]: T = query.uniqueResult.asInstanceOf[T]

  def asList[T]: List[T] = query.list.asInstanceOf[java.util.List[T]].toList

  def headOption[T]:Option[T] = {
    query.setMaxResults(1)
    asList[T].headOption
  }

  def apply(params: (String, Any)*) = withParams(params :_*)
}

class DeletableQuery[T](session:Session)(implicit entityType:Manifest[T]) {

  import Cond.applyRule

  private type Myself = DeletableQuery[T]
  private type Condition = (T) => Cond
  private var query = "from " + entityType.erasure.getName
  private val params = scala.collection.mutable.Map[String, Any]()

  def find(f: Condition): Option[T] = {
    filter(f)
    val q = createQuery("select")
    new PimpedQuery(q.setMaxResults(1)).headOption
  }

  def filter(f: Condition) = {
    val rule = applyRule(f)
    if(params.isEmpty) {
      query += " where "
    } else {
      query += " and "
    }
    query += rule.content
    params.put(rule.params._1, rule.params._2)
    this
  }

  def delete = createQuery("delete").executeUpdate

  private def createQuery(prefix:String) = {
    val q = session.createQuery(prefix + " " + query)
    params.foreach((p:Pair[String,Any]) => q.setParameter(p._1, p._2))
    q
  }
}
