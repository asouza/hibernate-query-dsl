package br.com.caelum.hibernatequerydsl

import org.hibernate.Session
import org.hibernate.cfg.Configuration
import org.junit.{After, Before}

trait SessionBased {
  protected var session:Session = _

  def newUser(name:String=null,age:Int=0, street:String=null) = {
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
     user
   }
   def withUser(name:String=null,age:Int=0,street:String=null) = {
     newUser(name,age,street)
     this
   }

   def and(name:String=null,age:Int=0, street:String=null) = {
     withUser(name, age, street)
   }

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


}