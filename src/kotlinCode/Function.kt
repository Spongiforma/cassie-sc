package kotlinCode

abstract class Function(
		val symbol: String
) : Atom(){
	override fun toString(): String {
		return symbol
	}
	class Negative : Function("-")
	class Sine : Function("sin")
	class Cosine : Function("cos")
	class Tangent : Function("tan")
}

