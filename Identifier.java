/**
 * identifer class to store variables in SimpleCalc.java
 * @author	Rishi Salvi
 * @since	March 27, 2024
 */

public class Identifier{
	private String name; 
	private double value; 
	public Identifier(String n, double v){
		name = n; 
		value = v; 
	}
	
	public String getName(){ return name; }
	
	public double getValue(){ return value; }
	
	public void setValue(double newV){
		if (!name.equals("pi") && !name.equals("e"))
			value = newV; 
	}
}
