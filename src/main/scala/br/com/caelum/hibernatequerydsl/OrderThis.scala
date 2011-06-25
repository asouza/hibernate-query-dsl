package br.com.caelum.hibernatequerydsl

import org.hibernate.criterion.Order

class OrderThis[T,P](path:String, val pimped:PimpedCriteria[T,P]) {

  import pimped.criteriaToPimped
	def asc():PimpedCriteria[T,P] = {
		pimped.criteria.addOrder(Order.asc(path))
	}
	def desc():PimpedCriteria[T,P] = {
		pimped.criteria.addOrder(Order.desc(path))
	}

}
