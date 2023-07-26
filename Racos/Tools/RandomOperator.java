package Racos.Tools;

import java.util.Random;

public class RandomOperator {
	
	Random random;
	
	public RandomOperator(){
		random = new Random();
	}
	
	/**
	 * get an integer in [lower,upper] under uniform distribution
	 * 
	 * @param lower 
	 * @param upper
	 * @return an integer within [lower,upper]
	 */
	public int getInteger(int lower, int upper){
		int b = upper-lower;
		return lower+random.nextInt(b+1);
	}
	
	/**
	 * get a double in [lower,upper] under uniform distribution
	 * 
	 * @param lower
	 * @param upper
	 * @return a double within [lower,upper]
	 */
	public double getDouble(double lower, double upper){
		double b = upper - lower;
		return lower+random.nextDouble()*b;
	}
	
	/**
	 * get a random number under standard normal distribution
	 * 
	 * @return a double number
	 */
	public double getGaussian(){
		Random random = new Random();
		return random.nextGaussian();
	}
	
	/**
	 * get a random number under normal distribution with mean and variance
	 * 
	 * @param mean
	 * @param variance
	 * @return a double number
	 */
	public double getGaussion(double mean, double variance){
		Random random = new Random();
		return Math.sqrt(variance)*random.nextGaussian()+mean;
	}

}
