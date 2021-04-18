package kotlinCode

abstract class Operator(
		val precedence: Int,
		val associativity: Associativity,
		val symbol: String
) : Atom(), Comparable<Operator> {
	override fun toString(): String {
		return symbol
	}
	override fun compareTo(other: Operator): Int {
		return precedence - other.precedence
	}
	class Times : Operator(500, Associativity.LEFT, "*")
	class Divide : Operator(500, Associativity.LEFT,"/")
	class Plus : Operator(300, Associativity.LEFT,"+")
	class Minus : Operator(300, Associativity.LEFT,"-")
	class Expn : Operator(600, Associativity.RIGHT,"expn")
	class LeftBracket : Operator(0, Associativity.NONE,"(")
	class RightBracket : Operator(0, Associativity.NONE,")")
}