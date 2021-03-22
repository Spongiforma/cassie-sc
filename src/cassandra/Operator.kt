package cassandra

abstract class Operator(
	val name: String,
	val precedence: Int
) : Comparable<Operator> {
	override fun compareTo(other: Operator): Int {
		return precedence - other.precedence
	}
}

class Times(name: String,precedence: Int) : Operator(name,precedence) {

}