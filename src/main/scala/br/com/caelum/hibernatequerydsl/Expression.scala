package br.com.caelum.hibernatequerydsl

import net.sf.cglib.proxy._
import java.lang.reflect.{ Method, Modifier }
object Expression {
  val callback = () => new MethodInterceptor {
    implicit def string2WithRubyPowers(str: String) = new {
      def withFirstCharLowered = {
        str.substring(0, 1).toLowerCase + str.substring(1, str.length)
      }
    }

    val properties = scala.collection.mutable.ListBuffer[String]()
    def intercept(proxiedObject: Any, method: Method, params: Array[Object], methodProxy: MethodProxy) = {
      val GetterExpression = """(get)?(\w*){1}""".r
      method.getName match {
        case GetterExpression(_, part2) => {
          if (part2 != "toString") {
            properties += part2.withFirstCharLowered
          }
        }
      }
      if ((method.getReturnType.getModifiers & Modifier.FINAL) == 0) {
        proxy(method.getReturnType, this)
      } else {
        println(properties.mkString("."))
        null
      }
    }
  }
  private def proxy[T](klass: Class[_], methodInterceptor: MethodInterceptor = callback()): T = {
    val enhancer = new Enhancer
    enhancer.setSuperclass(klass)
    enhancer.setCallback(methodInterceptor)
    enhancer.create.asInstanceOf[T]
  }
  def exp[T](implicit manifest: Manifest[T]): T = {
    proxy(manifest.erasure)
  }
}
