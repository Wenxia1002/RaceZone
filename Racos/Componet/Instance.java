package Racos.Componet;

public class Instance {
	//the value in each dimension
	private double[] feature;
	//the objective function value with the feature
	private double value;

	public double [][] region;
	/**
	 * constructor with parameter class Dimension
	 * 
	 * @param dim, the massage of dimension
	 */
	public Instance(Dimension dim){
		feature = new double[dim.getSize()];
		value = 0;
		region = new double[dim.getSize()][2];
		for(int i = 0;i < dim.getSize();++i){
			region[i][0] = dim.getRegion(i)[0];
			region[i][1] = dim.getRegion(i)[1];
		}
	}
	
	/**
	 * constructor with parameter dimensionsize
	 * user can construct class instance with dimension size
	 * 
	 * @param dimensionsize
	 */
	public Instance(int dimensionsize){
		feature = new double[dimensionsize];
		value = 0;
	}
	
	/**
	 * get feature value in one dimension
	 * 
	 * @param index
	 * @return the index-th dimension's feature value
	 */
	public double getFeature(int index){
		return feature[index];
	}
	
	/**
	 * function getFeature without parameter
	 * 
	 * @return all feature values
	 */
	public double[] getFeature(){
		return feature;
	}
	
	/**
	 * without index, in this case, each feature has the same value
	 * 
	 * @param fea, the feature value
	 */
	public void setFeature(double fea){
		for(int i=0; i<feature.length; i++){
			feature[i] = fea;
		}
		return ;
	}
	
	/**
	 * with index parameter, in this case, setting index-th feature value only
	 * 
	 * @param index
	 * @param fea, the feature value
	 */
	public void setFeature(int index, double fea){
		feature[index] = fea;
	}
	
	/**
	 * 
	 * 
	 * @return the objective function value in this feature
	 */
	public double getValue(){
		return value;
	}
	
	/**
	 * setting the objective function value in this feature
	 * 
	 * @param val, the objective function value
	 */
	public void setValue(double val){
		value = val;
	}
	
	/**
	 * get a copy of this instance
	 * 
	 * @return
	 */
	public Instance CopyInstance(){
		Instance copy = new Instance(feature.length);
		for(int i=0; i<feature.length; i++){
			copy.setFeature(i, feature[i]);
		}
		copy.setValue(value);
		return copy;		
	}
	
	/**
	 * if exist one feature in this instance is different from corresponding feature in ins, return false
	 * 
	 * @param ins
	 * @return
	 */
	public boolean Equal(Instance ins){
		for(int i=0; i<feature.length; i++){
			if(feature[i]!=ins.getFeature(i)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * show instance
	 */
	public void PrintInstance(){
		for(int i=0; i<feature.length; i++){
			System.out.print(feature[i]+" ");
		}/**/
		System.out.println(":"+value);
	}

}
