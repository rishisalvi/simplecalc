import java.util.List;		// used by expression evaluator
import java.util.ArrayList;
/**
 *	a simple arithmetic calculator that receives an expression from the user
 *	and performs the given operations to calculate a sum
 *	deals with +, -, *, /, %, and ^ as well as ( and ) -> basic functions of a 
 *	calculator and has to follow PEMDAS and general arithmetic rules
 *	breaks down the equation into its separate parts and uses two stacks to 
 *	compute the answer using FILO 
 *
 *	@author	Rishi Salvi
 *	@since	February 27, 2024
 */
public class SimpleCalc {
	
	private ExprUtils utils;	// expression utilities
	
	private ArrayStack<Double> valueStack;		// value stack
	private ArrayStack<String> operatorStack;	// operator stack
	private ArrayList<Identifier> variables; 	// variable database

	// constructor	
	public SimpleCalc() {
		utils = new ExprUtils();
		valueStack = new ArrayStack<Double>(); 
		operatorStack = new ArrayStack<String>(); 
		variables = new ArrayList<>(); 
		variables.add(new Identifier("e", Math.E)); 
		variables.add(new Identifier("pi", Math.PI)); 
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
		List<String> tokens = null; 
		do{
			expression = Prompt.getString(" "); 
			if (expression.equals("h")){
				printHelp(); 
				System.out.println();
			}
			else if (expression.equals("l")){
				printVariables(); 
			}
			else if (!expression.equals("q")){ // if not quit{
				tokens = utils.tokenizeExpression(expression); 
				double answer = evaluateExpression(tokens); 
				if (tokens.size() > 2 && tokens.get(1).equals("="))
					System.out.printf("   %-8s= %f%n", tokens.get(0) + " ", answer);
				else
					System.out.println(answer); 
			}
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
	
	/**Print variables */
	public void printVariables() {
		System.out.println("Variables:");
		for (int i = 0; i < variables.size(); i++)
			System.out.printf("   %-8s= %3f%n", variables.get(i).getName(), 
				variables.get(i).getValue());
	}
	
	/**
	 *	Evaluate expression and return the value
	 *	@param tokens	a List of String tokens making up an arithmetic expression
	 *	@return			a double value of the evaluated expression
	 */
	public double evaluateExpression(List<String> tokens) {
		double value = 0; // answer
		for (int i = 0; i < tokens.size(); i++){ // adds all tokens to stacks
			if (i == 0 && tokens.size() > 2 && tokens.get(1).equals("="))
				i = 2; 
			String token = tokens.get(i); 
			if (token.length() == 1 && utils.isOperator(token.charAt(0))){ // if operator
				/* if start of paratheses, just add to stack immediately */
				if (token.indexOf("(") != -1)
					operatorStack.push(token);
				/* if end of paratheses, solve the contents of the paratheses and discard it */
				else if (token.indexOf(")") != -1){
					solveParantheses(); 
					if (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")
						&& !operatorStack.peek().equals("+") && !operatorStack.peek().equals("-")){
						//check if immediate calculations (*, /, %) can be done prior to paratheses
						calculate(operatorStack.pop(), valueStack.pop());
					}
				}
				else if (token.equals("^")){ // exponents (right to left)
					operatorStack.push(token);
					i = solveExponents(tokens, i); 
					if (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")
						&& !operatorStack.peek().equals("+") && !operatorStack.peek().equals("-")){
						//check if immediate calculations (*, /, %) can be done prior to paratheses
						calculate(operatorStack.pop(), valueStack.pop());
					}
				}
				else{ // any other operator
					String check = "+"; 
					if (!operatorStack.isEmpty()) // prevent error
						check = operatorStack.peek(); 
					/* if token is not addition or subtraction, perform operation immediately */
					if (hasPrecedence(check, token) && !token.equals("+") && !token.equals("-")){
						if (tokens.get(i + 1).equals("(")) // pause if next token is paratheses
							operatorStack.push(token);
						else if (i + 2 < tokens.size() && tokens.get(i + 2).equals("^"))
							operatorStack.push(token); // pause if next operator is exponent
						else{
							calculate(token, getNumber(tokens.get(i + 1)));
							i++; // already added the next token in prior step
						}
					}
					else{ // operator is addition/subtraction and will be added later
						operatorStack.push(token);
					}
				}
			}
			else
				valueStack.push(getNumber(token));
		}
		solveEquation(); // solve the rest of the equation (only addition and subtraction)
		value = valueStack.pop();
		if (tokens.size() > 2 && tokens.get(1).equals("=")){
			boolean found = false; 
			for (int j = 0; j < variables.size(); j++){
				if (variables.get(j).getName().equals(tokens.get(0))){
					variables.get(j).setValue(value);
					found = true;
				}
			}
			if (!found)
				variables.add(new Identifier(tokens.get(0), value)); 
		}
		return value;
	}
	
	public double getNumber(String token){
		if (Character.isDigit(token.charAt(0))) // number (just added to valueStack)
			return Double.parseDouble(token);
		else{
			for (int j = 0; j < variables.size(); j++){
				if (variables.get(j).getName().equals(token)){
					return variables.get(j).getValue();
				}
			}
			variables.add(new Identifier(token, 0.0)); 
			return 0.0; 
		}
	}

	/**
	 * peforms operations for two given operands and their operator symbol because
	 * the symbol is originally a String so the operation cannot be done
	 * directly 
	 * @param operation		what the symbol for the operation is 
	 * @param operand		the second number in the expression
	 */
	public void calculate(String operation, double op){
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
		else // exponent (never goes here)
			valueStack.push(Math.pow(valueStack.pop(), op));
	}

	/**
	 * solves the contents of the parantheses (between the brackets) so they can 
	 * be removed and treated as a single number
	 * at this point, all of the immediate operators (*, /, %, ^) have already been
	 * done, so just + and - remain, meaning the total can easily be computed
	 * using LIFO
	 */
	public void solveParantheses(){ 
		String token = "";
		double total = 0.0;  
		while (!token.equals("(")){ // until reaching the opening bracket
			double temp = valueStack.pop(); 
			token = operatorStack.pop(); 
			if (token.equals("-")) // make a negative number
				temp = temp * -1; 
			total += temp; 
		}
		valueStack.push(total); // final value
	}

	/**
	 * since exponents are solved right to left, they cannot be treated like any
	 * other operator - but instead something like paratheses
	 * the entire exponent hierachy is added to the stack until the appearance
	 * of the first nonexponent operator or the end of the expression
	 * the exponents are calculated using LIFO, as that means the right is calculated
	 * first
	 * @param tokens		the ArrayList that has every part of the expression
	 * @param index			the index with the first occurence of the exponent operator
	 * @return				the next index after the entire exponent has been calculated
	 */
	public int solveExponents(List<String> tokens, int index){
		int counter = 1; // additional indexes
		/** until first nonexponent or end of expression */
		while (operatorStack.peek().equals("^") && counter + index < tokens.size()){
			String temp = tokens.get(index + counter); 
			if (counter % 2 == 1) // if number (every other token)
				valueStack.push(Double.parseDouble(temp));
			else
				operatorStack.push(temp); 
			counter++; 
		}
		/** ending early indicates finding a nonexponent operator and adding it to
		 * the stack
		 * need to subtract 2 - 1 because you need to go one index higher to find
		 * the nonexponent and 1 more because that loop part increments counter
		 */
		if (!operatorStack.peek().equals("^")){
			operatorStack.pop(); // remove nonexponent
			counter -= 2; 
		}

		/** while exponent operator is not done or expression is not empty */
		while (!operatorStack.isEmpty() && operatorStack.peek().equals("^")){
			double exp = valueStack.pop(); 
			double base = valueStack.pop(); 
			exp = Math.pow(base, exp);
			operatorStack.pop(); 
			valueStack.push(exp); // push temp value back on top
		}
		return counter + index;
	}

	/**
	 * solves the contents of the remaing expression so all of the operators can 
	 * be removed and treated as a single number
	 * at this point, all of the immediate operators (*, /, %, ^) have already been
	 * done, so just + and - remain, meaning the total can easily be computed
	 * using LIFO
	 */
	public void solveEquation(){
		String token = "";
		double total = 0.0;  
		while (!operatorStack.isEmpty()){
			double temp = valueStack.pop(); 
			token = operatorStack.pop(); 
			if (token.equals("-")) // negative
				temp = temp * -1; 
			total += temp; 
		}
		total += valueStack.pop(); // add the last value (1 more value than operator)
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
