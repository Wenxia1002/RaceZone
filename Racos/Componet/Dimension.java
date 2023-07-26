package Racos.Componet;

public class Dimension {
	
	private int size;          // the dimension size
	private double[][] region; //the feasible region in each dimension, region[][0] is lower bound, region[][1] is upper bound
	private boolean[] type;    //the type in each dimension, true means continue, false means discrete

	public Dimension(){
		size = 1;
		region = new double[1][2];
		type = new boolean[1];
		setDimension(0,1,true);
		
	}

	/**
	 * setting dimension size
	 * @param s
	 */
	public void setSize(int s){
		size = s;
		region = new double[size][2];
		type = new boolean[size];
	}
	
	 /**
	  * setting dimension, in this case, each dimension has the same setting
	  * 
	  * @param lower, the region's lower bound
	  * @param upper, the region's upper bound
	  * @param t, the dimension's type
	  */
	public void setDimension(double lower, double upper, boolean t){
		for(int i=0; i<size; i++){
			region[i][0] = lower;
			region[i][1] = upper;
			type[i] = t;
		}
		return ;
	}
	
	/**
	  * setting the index-th dimension only
	  * 
	  * @param index, the index-th dimension
	  * @param lower, the region's lower bound
	  * @param upper, the region's upper bound
	  * @param t, the dimension's type
	  */
	public void setDimension(int index, double lower, double upper, boolean t){
		region[index][0] = lower;
		region[index][1] = upper;
		type[index] = t;
		return ;
	}
	
	/**
	 * 
	 * 
	 * @return dimension size
	 */
	public int getSize(){
		return size;
	}
	
	/**
	 * return feasible region in one dimension
	 * 
	 * @param index, which dimension
	 * @return the feasible region in index-th dimension
	 */
	public double[] getRegion(int index){
		return region[index];
	}
	
	/**
	 * return the type
	 * 
	 * @param index, which dimension
	 * @return the type of index-th dimension
	 */
	public boolean getType(int index){
		return type[index];
	}
	
	public void PrintDim(){
		for(int i=0; i<size; i++){
			System.out.print("["+region[i][0]+","+region[i][1]+"] ");
		}
		System.out.println();
	}
	
	
}
