package br.com.caelum.hibernatequerydsl.conditions

class AnyConditioner(x: Any) {

  /**
   * Checks that a field is equal to a specific value,
   */
  def equal(value: Any) = new EqCond(Hacker.field, value)
  /**
   * Checks that a field is equal to a specific value,
   */
  def ===(value: Any) = equal(value)

  /**
   * Checks that a field is null.
   */
  def isNull() = new IsNull(Hacker.field)

}








