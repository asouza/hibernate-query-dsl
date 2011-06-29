package br.com.caelum.hibernatequerydsl

import org.hibernate.Session
import org.hibernate.cfg.Configuration
import org.junit.{Test, After, Before}
import org.junit.Assert._
import br.com.caelum.hibernatequerydsl.PimpedSession._
import br.com.caelum.hibernatequerydsl.TypeSafe._
class ActiveCollectionAcceptanceTest {

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

  private def withUser(name:String=null,age:Int=0, street:String=null) = {
    val user = new User
    user setName name
    user setAge  age
    session.save(user)
    if(street!=null){
      val address = new Address
      address setStreet street
      address setUser  user
      session.save(address)
    }
    this
  }

  private def and(name:String=null,age:Int=0, street:String=null) = {
    withUser(name, age, street)
  }

  def ar = new ActiveCollection[User](null, session.from[User])

  @Test
  def shouldSupportTake {
    withUser("guilherme").and("alberto")
    val users = ar.take(1)
    assertEquals("guilherme", users(0).getName)
    assertEquals(1, users.size)
  }

  @Test
  def shouldSupportParametersCombinedWithTake {
    withUser("guilherme").and("alberto")
    val users = ar.filter(_.getName equal "alberto").take(1)
    assertEquals("alberto", users(0).getName)
    assertEquals(1, users.size)
  }


  @Test
  def shouldSupportFindingAnElement {
    withUser("guilherme", 29).and("alberto").and("guilherme", 30)
    val users = ar.find(_.getName equal "guilherme")
    assertEquals(29, users.get.getAge)
  }

  @Test
  def shouldSupportDroppingSomething {
    withUser("guilherme", 29).and("alberto").and("guilherme", 30)
    val users = ar.filter(_.getName equal "guilherme").drop(1)
    assertEquals(30, users.head.getAge)
  }



  @Test
  def shouldSupportCheckingIfAnElementExists {
    withUser("guilherme").and("alberto")
    assertTrue(ar.exists(_.getName equal "alberto"))
    assertFalse(ar.exists(_.getName equal "marcos"))
  }

  @Test
  def shouldSupportGrabbingAll {
    withUser("guilherme").and("alberto")
    val users:List[User] = ar
    assertEquals(2, users.size)
  }

  @Test
  def shouldSupportRegrabbing {
    withUser("guilherme").and("alberto")
    val users = ar
    assertEquals(2, users.size)
    assertEquals(classOf[ActiveCollection[User]], ar.getClass)
    assertEquals(1, users.take(1).size)
  }

}