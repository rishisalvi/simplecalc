import java.util.List;		// used by expression evaluator

/**
 *	<Description goes here>
 *
 *	@author	
 *	@since	
 */
public class SimpleCalc {
	
	private ExprUtils utils;	// expression utilities
	
	private ArrayStack<Double> valueStack;		// value stack
	private ArrayStack<String> operatorStack;	// operator stack

	// constructor	
	public SimpleCalc() {
		utils = new ExprUtils();
		valueStack = new ArrayStack<Double>(); 
		operatorStack = new ArrayStack<String>(); 
	}
	
	public static void main(String[] args) {
		SimpleCalc sc = new SimpleCalc();
		sc.run();
	}
	
	public void run() {
		System.out.println("\nWelcome to SimpleCalc!!!");
		runCalc();
		System.out.println("\nThanks for using SimpleCalc! Goodbye.\n");
	}
	
	/**
	 *	Prompt the user for expressions, run the expression evaluator,
	 *	and display the answer.
	 */
	public void runCalc() {
		String expression = ""; 
		do{
			expression = Prompt.getString(" "); 
			if (expression.equals("h")){
				printHelp(); 
				System.out.println();
			}
			else if (!expression.equals("q"))
				System.out.println(evaluateExpression(utils.tokenizeExpression(expression))); 
		} while (!expression.equals("q"));
	}
	
	/**	Print help */
	public void printHelp() {
		System.out.println("Help:");
		System.out.println("  h - this message\n  q - quit\n");
		System.out.println("Expressions can contain:");
		System.out.println("  integers or decimal numbers");
		System.out.println("  arithmetic operators +, -, *, /, %, ^");
		System.out.println("  parentheses '(' and ')'");
	}
	
	/**
	 *	Evaluate expression and return the value
	 *	@param tokens	a List of String tokens making up an arithmetic expression
	 *	@return			a double value of the evaluated expression
	 */
	public double evaluateExpression(List<String> tokens) {
		double value = 0;
		for (int i = 0; i < tokens.size(); i++){
			String token = tokens.get(i); 
			if (token.length() == 1 && utils.isOperator(token.charAt(0))){
				if (token.indexOf("(") != -1)
					operatorStack.push(token);
				else if (token.indexOf(")") != -1){
					solveParantheses(); 
					if (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")
						&& !operatorStack.peek().equals("+") && !operatorStack.peek().equals("-")){
						calculate(operatorStack.pop(), "" + valueStack.pop());
					}
				}
				else if (token.equals("^")){
					operatorStack.push(token);
					i = solveExponents(tokens, i); 
				}
				else{
					String check = "+"; 
					if (!operatorStack.isEmpty())
						check = operatorStack.peek(); 
					if (hasPrecedence(check, token) && !token.equals("+") && !token.equals("-")){
						if (tokens.get(i + 1).equals("("))
							operatorStack.push(token);
						else{
							calculate(token, tokens.get(i + 1));
							i++; 
						}
					}
					else{
						operatorStack.push(token);
					}
				}
			}
			else{
				valueStack.push(Double.parseDouble(token));
			}
		}
		solveEquation();
		value = valueStack.pop();
		return value;
	}

	public void calculate(String operation, String operand){
		double op = Double.parseDouble(operand);
		if (operation.equals("+"))
			valueStack.push(valueStack.pop() + op);
		else if (operation.equals("-"))
			valueStack.push(valueStack.pop() - op);
		else if (operation.equals("*"))
			valueStack.push(valueStack.pop() * op);
		else if (operation.equals("/"))
			valueStack.push(valueStack.pop() / op);
		else if (operation.equals("%")){
			valueStack.push(valueStack.pop() % op);
		}
		else
			valueStack.push(Math.pow(valueStack.pop(), op));
	}

	public void solveParantheses(){
		String token = "";
		double total = 0.0;  
		while (!token.equals("(")){
			double temp = valueStack.pop(); 
			token = operatorStack.pop(); 
			if (token.equals("-"))
				temp = temp * -1; 
			total += temp; 
		}
		valueStack.push(total);
	}

	public int solveExponents(List<String> tokens, int index){
		int counter = 1; 
		while (operatorStack.peek().equals("^") && counter + index < tokens.size()){
			String temp = tokens.get(index + counter); 
			if (counter % 2 == 1)
				valueStack.push(Double.parseDouble(temp));
			else
				operatorStack.push(temp); 
			counter++; 
		}
		if (counter + index != tokens.size()){
			operatorStack.pop();
			counter -= 2; 
		}

		while (!operatorStack.isEmpty() && operatorStack.peek().equals("^")){
			double exp = valueStack.pop(); 
			double base = valueStack.pop(); 
			exp = Math.pow(base, exp);
			operatorStack.pop(); 
			valueStack.push(exp);
		}
		return counter + index;
	}

	public void solveEquation(){
		String token = "";
		double total = 0.0;  
		while (!operatorStack.isEmpty()){
			double temp = valueStack.pop(); 
			token = operatorStack.pop(); 
			if (token.equals("-"))
				temp = temp * -1; 
			total += temp; 
		}
		total += valueStack.pop(); 
		valueStack.push(total);
	}
	
	/**
	 *	Precedence of operators
	 *	@param op1	operator 1
	 *	@param op2	operator 2
	 *	@return		true if op2 has higher or same precedence as op1; false otherwise
	 *	Algorithm:
	 *		if op1 is exponent, then false
	 *		if op2 is either left or right parenthesis, then false
	 *		if op1 is multiplication or division or modulus and 
	 *				op2 is addition or subtraction, then false
	 *		otherwise true
	 */
	private boolean hasPrecedence(String op1, String op2) {
		if (op1.equals("^")) return false;
		if (op2.equals("(") || op2.equals(")")) return false;
		if ((op1.equals("*") || op1.equals("/") || op1.equals("%")) 
				&& (op2.equals("+") || op2.equals("-")))
			return false;
		return true;
	}
	 
}
