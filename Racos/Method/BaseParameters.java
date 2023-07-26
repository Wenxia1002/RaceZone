package Racos.Method;

public class BaseParameters {

    protected int sampleSize;
    protected int maxIteration;
    protected int budget;
    protected int positiveNum;
    protected double randProbability;
    protected int uncertainBits;

    public BaseParameters() {
    }

    public void setSampleSize(int size) {
        sampleSize = size;
    }

    public void setMaxIteration(int max) {
        maxIteration = max;
    }

    public void setBudget(int bud) {
        budget = bud;
    }

    public void setPositiveNum(int num) {
        positiveNum = num;
    }

    public void setRandProbability(double pro) {
        randProbability = pro;
    }

    public void setUncertainBits(int unc) {
        uncertainBits = unc;
    }

}
