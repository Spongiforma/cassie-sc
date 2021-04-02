package cassandra

abstract class Atom{
	companion object {
		fun typeValue(inp: String) : Atom{
			return when(inp){
				"+" -> Operator.Plus()
				"-" -> Operator.Minus()
				"*" -> Operator.Times()
				"/" -> Operator.Divide()
				"(" -> Operator.LeftBracket()
				")" -> Operator.RightBracket()
				"^" -> Operator.Expn()
				else -> NumberAtom(inp)
			}
		}

	}
	class NumberAtom (
			val value: String
	) : Atom(){
		override fun toString(): String {
			return value
		}
	}
	enum class Associativity{
		LEFT, RIGHT, NONE
	}
}

