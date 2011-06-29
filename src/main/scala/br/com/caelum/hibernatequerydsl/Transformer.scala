package br.com.caelum.hibernatequerydsl

import org.hibernate.Criteria

class Transformer[T,P](criteria: Criteria) {
  def asList = new PimpedCriteria[T,P]("", criteria).asList[T]

  def unique = new PimpedCriteria[T,P]("", criteria).unique[T]
}
