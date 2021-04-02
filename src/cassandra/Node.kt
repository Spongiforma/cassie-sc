package cassandra

class Node(
	val name: Atom,
	val left: Node?,
	val right: Node?
){
	fun isLeaf() : Boolean {
		return left==null && right == null && name !is Operator
	}

	override fun toString(): String {
		return if(isLeaf())
			name.toString()
		else
			"($name $left $right)"
	}
}

//class Sexpression(
//		procedure: Atom,
//		val arg1: Node,
//		val arg2: Node
//) : Node(procedure)
//
//class Leaf(
//		val numberAtom: Atom.NumberAtom
//) : Node(numberAtom)