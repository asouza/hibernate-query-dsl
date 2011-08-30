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

class TypeSafeQuery[T](session:Session)(implicit entityType:Manifest[T]) {

  import Cond.applyRule

  private type Myself = TypeSafeQuery[T]
  private type Condition = (T) => Cond
  private var query = "from " + entityType.erasure.getName
  private val params = scala.collection.mutable.Map[String, Any]()
  override def toString = "[query " + entityType + " " + query + "]"

  /**
   * Applies a filter that will return only one result (Option on it!)
   */
  def find(f: Condition): Option[T] = {
    filter(f)
    val q = createQuery("select")
    new PimpedQuery(q.setMaxResults(1)).headOption
  }

  /**
   * Adds a new filter to this query. Still do not execute the query.
   */
  def filter(f: Condition) = {
    val rule = applyRule(f)
    if(params.isEmpty) {
      query += " where "
    } else {
      query += " and "
    }
    val count = params.size + 1
    query += rule.content(count)
    params ++= rule.params(count)
    this
  }

  /** Deletes all entries that would otherwise be returned by this query. Lazy delete. */
  def delete = createQuery("delete").executeUpdate

  /**
   * Forces the execution of this query and returns it as a list
   */
  def list: List[T] = createQuery("").list.asInstanceOf[java.util.List[T]].toList

  private def createQuery(prefix:String) = {
    val q = session.createQuery(prefix + " " + query)
    params.foreach((p:Pair[String,Any]) => q.setParameter(p._1, p._2))
    q
  }
}
