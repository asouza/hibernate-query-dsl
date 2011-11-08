package br.com.caelum.hibernatequerydsl

import org.hibernate.criterion.Order

// TODO if the method to construct this guy received the asc or desc as a second
// parameter thourhg a import, then there would be no need for this extra guy or the
// extra implicit. remove it?
class OrderThis[T,P](path:String, val pimped:PimpedCriteria[T,P]) {

  import pimped.criteriaToPimped
	def asc():PimpedCriteria[T,P] = {
		pimped.criteria.addOrder(Order.asc(path))
	}
	def desc():PimpedCriteria[T,P] = {
		pimped.criteria.addOrder(Order.desc(path))
	}

}
