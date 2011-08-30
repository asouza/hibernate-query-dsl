package br.com.caelum.hibernatequerydsl

import conditions.{AnyConditioner, EqCond}
import org.hibernate.{ Session, Query }
import org.hibernate.criterion.{ Order, Restrictions, MatchMode, Projections }
import scala.reflect.{Apply, Select, Literal, Tree,Code,This }
import java.io.Serializable

object PimpedSession {
	
  implicit def session2PimpedSession(session: Session) = new PimpedSession(session)

  implicit def pimpedCriteria2Criteria[T,P](pimped: PimpedCriteria[T,P]) = pimped.criteria

  implicit def hibernateQuery2PimpedQuery(query: Query) = new PimpedQuery(query)
  
  implicit def code2PimpedCode[T](code:Code[T]) = new PimpedCode(code)
  
  implicit def code2String[T](code:Code[T]) = new PimpedCode(code).toString  

  implicit def orderThisToPimped[T,P](order:OrderThis[T,P]) = order.asc

  implicit def criteriaToActive[T](criteria:PimpedCriteria[T,T])(implicit t:Manifest[T]) = new ActiveCollection[T](null, criteria)

  implicit def acToList[T](ac:ActiveCollection[T]) = ac.grabThem

  implicit def queryToList[T](query:TypeSafeQuery[T]) = query.list

  implicit def sessionToQueriable(session: Session) = new {
    def query[T](implicit manifest:Manifest[T]) = new TypeSafeQuery[T](session)(manifest)
  }

}

object TypeUnsafe {
  implicit def string2PimpedStringCondition(field: String) = new PimpedStringCondition(field)
}
object TypeSafe {
  implicit def any2Conditioner(field: Any) = new AnyConditioner(field)

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
  
  def equal(value: Any) = Restrictions.eq(evaluate, value)

  def >(value: Any) = Restrictions.gt(evaluate, value)

  def >=(value: Any) = Restrictions.ge(evaluate, value)

  def <(value: Any) = Restrictions.lt(evaluate, value)

  def <=(value: Any) = Restrictions.le(evaluate, value)
  
  def !==(value: Any) = Restrictions.ne(evaluate,value)

  def like(value: String) = Restrictions.ilike(evaluate, value, MatchMode.ANYWHERE)

  def isNull = Restrictions.isNull(evaluate)

  def isNotNull = Restrictions.isNotNull(evaluate)

  def desc = Order.desc(evaluate)

  def asc = Order.asc(evaluate)

  def alias(newName: String) = Projections.property(evaluate).as(newName)  
}

class PimpedStringCondition(field: String) {
  def equal(value: Any) = Restrictions.eq(field, value)

  def >(value: Any) = Restrictions.gt(field, value)

  def >=(value: Any) = Restrictions.ge(field, value)

  def <(value: Any) = Restrictions.lt(field, value)

  def <=(value: Any) = Restrictions.le(field, value)
  
  def !==(value: Any) = Restrictions.ne(field,value)

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
	  new PimpedCriteria[T,T]("", criteria)
  }

  def query(query: String) = session.createQuery(query)

  def count[T](implicit manifest: Manifest[T]) = from[T].count

  def exists[T](implicit manifest: Manifest[T]) = count[T] > 0

  def first[T](implicit manifest: Manifest[T]) = from[T].first[T]

  def last[T](implicit manifest: Manifest[T]) = from[T].last[T]

  def load[T](implicit manifest: Manifest[T], id:Serializable) = {
    session.load(manifest.erasure, id).asInstanceOf[T]
  }

}



