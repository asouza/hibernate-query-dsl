package br.com.caelum.hibernatequerydsl

import org.junit.Test
import org.junit.Assert._
import br.com.caelum.hibernatequerydsl.TypeQuerySafe._

class TypeSafeQueryTest extends SessionBased{

  @Test
  def shouldSupportFiltering {
    withUser("guilherme", 30).and("aniche").and("alberto").and("guilherme", 29)
    val deleted = query.filter(_.getName equal "guilherme").delete
    assertEquals(2, deleted)
  }

  @Test
  def shouldSupportFilteringTypeSafeByType {
    val query = new TypeSafeQuery[Address](session)
    withUser("aniche").and("alberto")
    val guilherme = newUser("guilherme", 29, "rua vergueiro")
    val deleted = query.filter(_.getUser equal guilherme).delete
    assertEquals(1, deleted)
  }

  @Test
  def shouldSupportFilteringWithConditional {
    withUser("guilherme").and("aniche").and("alberto")
    val q = query.filter((u) => (u.getName equal "guilherme") || (u.getName equal "alberto"))
    println("Querying " + q)
    val deleted = q.delete
    assertEquals(2, deleted)
  }

  private def query = new TypeSafeQuery[User](session)
}