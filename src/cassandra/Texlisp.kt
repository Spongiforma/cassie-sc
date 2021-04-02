package cassandra
import java.lang.Character.isDigit
import java.lang.Character.isWhitespace
import java.util.*


object TexToLisp{
	private fun isDigitElement(input: Char) : Boolean {
        return input.isDigit() || setOf('.','-').contains(input)
    }

    /**
     * Atomize expression
     * Takes in valid human input of expression and splits it into atoms
     * @param expression
     * @return A list of atoms
     */
    private fun atomizeExpression(expression: String) : List<Atom> {
        if (expression.contains(" ")) // Strip whitespace
            return atomizeExpression(expression.filterNot{ isWhitespace(it) })

        var prev = 0
        val atoms = mutableListOf<String>()
        for (i in expression.indices) {
            if(i < expression.length-1 &&
                    (expression[i].isDigit() || expression[i] == '.') &&
                    (expression[i+1].isDigit() || expression[i+1] == '.')){
                continue
            }
            atoms.add(expression.substring(prev,i+1))
            prev = i+1
        }
        if(prev!=expression.length){
            atoms.add(expression.substring(prev))
        }
        return atoms.map{ Atom.typeValue(it) }
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
            stack.push(Node(operator,left,right))
        }
        for (atom in atoms){
            when (atom){
                is Atom.NumberAtom -> operandStack.push(Node(atom,null,null))
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
        return operandStack.pop()
    }

//    fun polishToTree(atoms: List<Atom>) : List<Int>{
//        val stack = Stack<Int>()
//        val par = mutableListOf<Int>()
//        stack.push(-1)
//        for (i in atoms.indices){
//            par.add(stack.pop())
//            if (atoms[i] is Operator){
//                stack.push(i)
//                stack.push(i)
//            }
//        }
//        return par
//    }
//
//    fun parentToAdjacencyList(atoms: List<Atom>, parent: List<Int>) : List<MutableList<Int>> {
//        val adj = List<MutableList<Int>>(parent.size/2) {mutableListOf()}
//        val edges = parent.mapIndexed{ v, p ->
//            p to v
//        }
//        for (edge in edges){
//            adj[edge.first].add(edge.second)
//        }
//        return adj
//    }



}
fun main(){
    while(true){
        val p = readLine()
        val ast =  TexToLisp.infixToAST(p!!)
        println(ast)
//        println(TexToLisp.polishToTree(polishAtoms).joinToString(","))
    }
}

