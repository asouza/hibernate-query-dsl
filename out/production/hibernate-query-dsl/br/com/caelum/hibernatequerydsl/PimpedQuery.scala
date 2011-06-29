package br.com.caelum.hibernatequerydsl

import org.hibernate.Query
import scala.collection.JavaConversions._

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
    query.setMaxResults(1).list.asInstanceOf[List[T]].headOption
  }
}
