package kotlinCode
import java.lang.Character.isWhitespace
import java.util.*

fun main(){
}

object TexLisp{

    fun evaluate(input: String, operation: String) : String {
//	    return MMAST.main(input,operation)
	    return ""
    }
	private fun isDigitElement(input: Char) : Boolean {
        return input.isDigit() || setOf('.').contains(input)
    }
    private fun unaryNegateTree(root: Node?) : Node? {
        return if(root==null){
           null
        } else if (root.isLeaf() && (root.name as Atom.NumberAtom).value.startsWith("-")){
            val value =(root.name as Atom.NumberAtom).value.drop(1)
            Node(Function.Negative(), Node(Atom.NumberAtom(value), null, null), null)
        } else {
            Node(root.name, unaryNegateTree(root.left), unaryNegateTree(root.right))
        }
    }

    /**
     * Atomize expression
     * Takes in valid human input of expression and splits it into atoms
     * @param expression
     * @return A list of atoms
     */
    fun atomizeExpression(expression: String) : List<Atom> {
        if (expression.contains(" ")) // Strip whitespace
            return atomizeExpression(expression.filterNot { isWhitespace(it) })

        var prev = 0
        val atoms = mutableListOf<String>()
        for (i in expression.indices) {
            if(i < expression.length-1 &&
                    isDigitElement(expression[i]) && isDigitElement(expression[i + 1])){
                continue
            }
            atoms.add(expression.substring(prev,i+1))
            prev = i+1
        }
        if(prev!=expression.length){
            atoms.add(expression.substring(prev))
        }

        val rem = mutableSetOf<Int>()
        return atoms.map{ Atom.typeValue(it) }
                .also {
                    for (i in 1..it.size - 3) {
                        if (it[i] is Operator && it[i + 1] is Operator.Minus && it[i + 2] is Atom.NumberAtom) {
                            val tmp = (it[i + 2] as Atom.NumberAtom).value
                            (it[i + 2] as Atom.NumberAtom).value = "-$tmp"
                            rem.add(i+1)
                        }
                    }
                }.filterIndexed { i,_ -> !rem.contains(i) }
    }

    /**
     * Converts expression in infix to polish prefix notation using Shunting Yard algorithm
     *
     * @param inp The expression in infix format
     * @return The output as a list of atom in polish prefix notation
     */
    fun infixToPrefix(inp: String) : List<Atom> {
        val atoms = atomizeExpression(inp)
                .reversed()
                .map { when(it){
                    is Operator.LeftBracket -> Operator.RightBracket()
                    is Operator.RightBracket -> Operator.LeftBracket()
                    else -> it
                }}
        val output = mutableListOf<Atom>()
        val op = Stack<Operator>()
        for (atom in atoms){
            when (atom){
                is Atom.NumberAtom -> output.add(atom)
                is Operator.LeftBracket -> op.push(atom)
                is Operator.RightBracket -> {
                    while(op.isNotEmpty() && op.peek() !is Operator.LeftBracket){
                        output.add(op.pop()) }
                    if (op.peek() is Operator.LeftBracket){
                        op.pop()
                    }
                }
                is Operator -> {
                    while(op.isNotEmpty() &&
                            (op.peek() > atom || (op.peek() == atom
                                    && op.peek().associativity == Atom.Associativity.RIGHT)) &&
                            op.peek() is Operator.LeftBracket){
                        output.add(op.pop())
                    }
                    op.push(atom)
                }
            }
        }
        while(op.isNotEmpty()){
            output.add(op.pop())
        }
        return output.reversed().toList()
    }

    /**
     * Converts expression in Infix to an AST using Shunting Yard algorithm
     *
     * @param inp
     * @return A node which is the root of the AST
     */
    fun infixToAST(inp: String) : Node {
        val atoms = atomizeExpression(inp)
        val operandStack = Stack<Node>()
        val operatorStack = Stack<Operator>()
        fun addNode(stack: Stack<Node>, operator: Atom){
            val right = stack.pop()
            val left = stack.pop()
            stack.push(Node(operator, left, right))
        }
        for (atom in atoms){
            when (atom){
                is Atom.NumberAtom -> operandStack.push(Node(atom, null, null))
                is Operator.LeftBracket -> operatorStack.push(atom)
                is Operator.RightBracket -> {
                    while(operatorStack.isNotEmpty() && operatorStack.peek() !is Operator.LeftBracket) {

                        addNode(operandStack, operatorStack.pop())
                    }
                    if(operatorStack.isNotEmpty() && operatorStack.peek() is Operator.LeftBracket) {
                        operatorStack.pop()
                    }
                }
                is Operator -> {
                    while(operatorStack.isNotEmpty()
                            && (operatorStack.peek() > atom
                                    || (operatorStack.peek() == atom
                                    && operatorStack.peek().associativity == Atom.Associativity.RIGHT))){
	                    addNode(operandStack,operatorStack.pop())
                    }
                    operatorStack.push(atom)
                }
            }
        }
        while(operatorStack.isNotEmpty()){
            addNode(operandStack,operatorStack.pop())
        }
        return unaryNegateTree(operandStack.pop())!!
    }
}

