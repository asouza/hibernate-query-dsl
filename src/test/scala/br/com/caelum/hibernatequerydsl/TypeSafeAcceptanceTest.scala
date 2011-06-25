package br.com.caelum.hibernatequerydsl

import org.hibernate.Session
import org.hibernate.cfg.Configuration
import org.junit.{Test, After, Before}
import org.junit.Assert._
import br.com.caelum.hibernatequerydsl.PimpedSession._
class TypeSafeAcceptanceTest {

  private var session:Session = _

  @Before
  def setUp {
    val cfg = new Configuration();
    session = cfg.configure().buildSessionFactory().openSession();
    session.beginTransaction();
  }

  @After
  def tearDown{
    if (session != null && session.getTransaction().isActive()) {
      session.getTransaction().rollback();
    }
  }

  private def withUser(name:String=null,age:Int=0) = {
    val user = new User
    user setName name
    user setAge  age
    session.save(user)
    this
  }
  private def and(name:String=null,age:Int=0) = {
    withUser(name, age)
  }

  @Test
  def shouldListAllObjects {
    withUser("guilherme").and("alberto")
    val users = session.from[User].orderBy2(_.getName).asc.list
    assertEquals("alberto", users.get(0).getName)
    assertEquals("guilherme", users.get(1).getName)
  }


  @Test
  def shouldSupportComingBackToCriteriaAndAgainToPimped {
    withUser("guilherme").and("alberto")
    val users = session.from[User].orderBy2(_.getName).asc.using(_.setMaxResults(1)).list
    assertEquals("alberto", users.get(0).getName)
    assertEquals(1, users.size())
  }

  @Test
  def shouldSupportOrderingByTwoElements {
    withUser("guilherme", 16).and("guilherme", 20)
    val users = session.from[User].orderBy2(_.getName).asc.orderBy2(_.getAge).desc.list
    assertEquals(20, users.get(0).getAge)
    assertEquals(16, users.get(1).getAge)
  }

}