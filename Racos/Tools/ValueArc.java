package Racos.Tools;

import java.util.ArrayList;

public class ValueArc{
    public double penalty;
    public double globalPenalty;
    public boolean sat;
    public double value;
    public double penAll;
    public int iterativeNum;
    public ArrayList<Double> arrayListBestValues;

    public ValueArc(double penalty,double globalPenalty,boolean sat){
        this.penalty = penalty;
        this.globalPenalty = globalPenalty;
        this.sat = sat;
        iterativeNum = 0;
    }

    public ValueArc(double value,boolean sat){
        this.value = value;
        this.sat = sat;
    }

    public ValueArc(){
        penAll = Double.MAX_VALUE;
        value = Double.MAX_VALUE;
    }
}
