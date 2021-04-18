package kotlinCode

abstract class Atom{
	companion object {
		fun typeValue(inp: String) : Atom {
			return when(inp){
				"+" -> Operator.Plus()
				"-" -> Operator.Minus()
				"*" -> Operator.Times()
				"/" -> Operator.Divide()
				"(" -> Operator.LeftBracket()
				")" -> Operator.RightBracket()
				"^" -> Operator.Expn()
				"\\sin" -> Function.Sine()
				"\\Cos" -> Function.Cosine()
				else -> NumberAtom(inp)
			}
		}
	}
	class NumberAtom (
			var value: String
	) : Atom(){
		override fun toString(): String {
			return value
		}
	}
	enum class Associativity{
		LEFT, RIGHT, NONE
	}
}

