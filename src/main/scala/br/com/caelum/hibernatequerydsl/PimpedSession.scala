package br.com.caelum.hibernatequerydsl

import org.hibernate.{ Criteria, Session, Query }
import org.hibernate.criterion.{ Order, Restrictions, MatchMode, Projections }
import scala.reflect.{Apply, Select, Literal, Tree,Code,This }

object PimpedSession {
	
  implicit def session2PimpedSession(session: Session) = new PimpedSession(session)

  implicit def pimpedCriteria2Criteria[T](pimped: PimpedCriteria[T]) = pimped.criteria

  implicit def string2PimpedStringCondition(field: String) = new PimpedStringCondition(field)

  implicit def hibernateQuery2PimpedQuery(query: Query) = new PimpedQuery(query)
  
  implicit def code2PimpedCode[T](code:Code[T]) = new PimpedCode(code)
  
  implicit def code2String[T](code:Code[T]) = new PimpedCode(code).toString  

  implicit def orderThisToPimped[T](order:OrderThis[T]) = order.asc
}

class PimpedCode[T](code: Code[T]) {

  implicit def string2WithRubyPowers(str: String) = new StringWithRubyPowers(str)
  
  class StringWithRubyPowers(str: String) {
    def withFirstCharLowered = {
      str.substring(0, 1).toLowerCase + str.substring(1, str.length)
    }
  }

  private def evaluate: String = {
    def extractString(tree: Tree, properties: List[String] = List()): List[String] = {
      //literal is for local variables and this for instance
      if (tree.isInstanceOf[Literal] ||tree.isInstanceOf[This] || tree.isInstanceOf[Select]) {
        return properties
      }
      
      val expressao = tree.asInstanceOf[Apply].fun.asInstanceOf[Select]
      val GetterExpression = """(get)?(\w*){1}""".r
      expressao.sym.name match {
        case GetterExpression(_, part2) => {
          extractString(expressao.qual, part2.withFirstCharLowered :: properties)
        }
      }
    }
    val tree = code.tree
    extractString(tree).mkString(".")
  } 
  
  override def toString = evaluate
  
  def equal(value: Object) = Restrictions.eq(evaluate, value)

  def >(value: Object) = Restrictions.gt(evaluate, value)

  def >=(value: Object) = Restrictions.ge(evaluate, value)

  def <(value: Object) = Restrictions.lt(evaluate, value)

  def <=(value: Object) = Restrictions.le(evaluate, value)
  
  def !==(value: Object) = Restrictions.ne(evaluate,value)

  def like(value: String) = Restrictions.ilike(evaluate, value, MatchMode.ANYWHERE)

  def isNull = Restrictions.isNull(evaluate)

  def isNotNull = Restrictions.isNotNull(evaluate)

  def desc = Order.desc(evaluate)

  def asc = Order.asc(evaluate)

  def alias(newName: String) = Projections.property(evaluate).as(newName)  
}

class PimpedQuery(query: Query) {
  def withParams(params: (String, Object)*) = {
    params.foreach((param) => {
      query.setParameter(param._1, param._2)
    })
    query
  }

  def unique[T]: T = query.uniqueResult.asInstanceOf[T]

  def asList[T]: java.util.List[T] = query.list.asInstanceOf[java.util.List[T]]
}

class PimpedStringCondition(field: String) {
  def equal(value: Object) = Restrictions.eq(field, value)

  def >(value: Object) = Restrictions.gt(field, value)

  def >=(value: Object) = Restrictions.ge(field, value)

  def <(value: Object) = Restrictions.lt(field, value)

  def <=(value: Object) = Restrictions.le(field, value)
  
  def !==(value: Object) = Restrictions.ne(field,value)

  def like(value: String) = Restrictions.ilike(field, value, MatchMode.ANYWHERE)

  def isNull = Restrictions.isNull(field)

  def isNotNull = Restrictions.isNotNull(field)

  def desc = Order.desc(field)

  def asc = Order.asc(field)

  def alias(newName: String) = Projections.property(field).as(newName)

}

class PimpedSession(session: Session) {

  def all[T](implicit manifest:Manifest[T]) = {
    from[T].list
  }

  def from[T](implicit manifest: Manifest[T]) = {
    val criteria = session.createCriteria(manifest.erasure)
	  new PimpedCriteria[T](criteria)
  }

  def query(query: String) = session.createQuery(query)

  def count[T](implicit manifest: Manifest[T]) = from[T].count

  def exists[T](implicit manifest: Manifest[T]) = count[T] > 0

  def first[T](implicit manifest: Manifest[T]) = from[T].first[T]

  def last[T](implicit manifest: Manifest[T]) = from[T].last[T]
}


class Transformer[T](criteria: Criteria) {
  def asList = new PimpedCriteria[T](criteria).asList[T]
  
  def unique = new PimpedCriteria[T](criteria).unique[T]
}

