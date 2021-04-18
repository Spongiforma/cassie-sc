package kotlinCode

class Node(
		val name: Atom,
		val left: Node?,
		val right: Node?
){
	fun isLeaf() : Boolean {
		return left==null && right == null && name !is Operator
	}
	fun isUnary() : Boolean {
		return left!=null && right == null && name is Function
	}
	fun isBinary() : Boolean {
		return left!=null && right != null && name is Operator
	}

	override fun toString(): String {
		return if(isLeaf())
			name.toString()
		else if (isUnary())
			"($name $left)"
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