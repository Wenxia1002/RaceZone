package Racos.Componet;

public class Instance {
	private double[] feature;
	private double value;

	public double [][] region;

	public Instance(Dimension dim){
		feature = new double[dim.getSize()];
		value = 0;
		region = new double[dim.getSize()][2];
		for(int i = 0;i < dim.getSize();++i){
			region[i][0] = dim.getRegion(i)[0];
			region[i][1] = dim.getRegion(i)[1];
		}
	}

	public Instance(int dimensionSize){
		feature = new double[dimensionSize];
		value = 0;
	}
	

	public double getFeature(int index){
		return feature[index];
	}
	

	public double[] getFeature(){
		return feature;
	}
	

	public void setFeature(double fea){
		for(int i=0; i<feature.length; i++){
			feature[i] = fea;
		}
		return ;
	}
	

	public void setFeature(int index, double fea){
		feature[index] = fea;
	}
	

	public double getValue(){
		return value;
	}
	

	public void setValue(double val){
		value = val;
	}

	public Instance CopyInstance(){
		Instance copy = new Instance(feature.length);
		for(int i=0; i<feature.length; i++){
			copy.setFeature(i, feature[i]);
		}
		copy.setValue(value);
		return copy;		
	}

	public boolean Equal(Instance ins){
		for(int i=0; i<feature.length; i++){
			if(feature[i]!=ins.getFeature(i)){
				return false;
			}
		}
		return true;
	}

	public void PrintInstance(){
		for(int i=0; i<feature.length; i++){
			System.out.print(feature[i]+" ");
		}/**/
		System.out.println(":"+value);
	}

}
