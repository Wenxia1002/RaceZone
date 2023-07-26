package Racos.Componet;

public class Dimension {
	
	private int size;
	private double[][] region;
	private boolean[] type;

	public Dimension(){
		size = 1;
		region = new double[1][2];
		type = new boolean[1];
		setDimension(0,1,true);
		
	}

	public void setSize(int s){
		size = s;
		region = new double[size][2];
		type = new boolean[size];
	}
	

	public void setDimension(double lower, double upper, boolean t){
		for(int i=0; i<size; i++){
			region[i][0] = lower;
			region[i][1] = upper;
			type[i] = t;
		}
	}

	public void setDimension(int index, double lower, double upper, boolean t){
		region[index][0] = lower;
		region[index][1] = upper;
		type[index] = t;
	}
	

	public int getSize(){
		return size;
	}
	

	public double[] getRegion(int index){
		return region[index];
	}
	

	public boolean getType(int index){
		return type[index];
	}
	
	public void printDim(){
		for(int i=0; i<size; i++){
			System.out.print("["+region[i][0]+","+region[i][1]+"] ");
		}
		System.out.println();
	}
	
	
}
