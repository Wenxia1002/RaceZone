package Racos.ObjectiveFunction;

import java.util.ArrayList;

import MPC.UserData;
import Racos.Componet.Dimension;
import Racos.Componet.Instance;
import Racos.Tools.ValueArc;


public class RaceZone implements Task {
    public final int featureSize;
    public ValueArc valueArc;
    private final Dimension dimensions;
    private ArrayList<UserData> userDataList;


    public RaceZone(ArrayList<UserData> userDataList, int size) {
        this.userDataList=userDataList;
        this.featureSize=size;
        this.dimensions = new Dimension();
        dimensions.setSize(size);
        for(int i=0;i<dimensions.getSize();i++){
            dimensions.setDimension(i,0,100,true);
        }

        valueArc=new ValueArc();
    }

    @Override
    public double getValue(Instance instance) {
        //读取测试集的数据
        //计算出分数
        double sum=0;
        for(UserData userData:userDataList){
            sum+=calculate(userData,instance);
        }

        return sum;
    }

    private double calculate(UserData userData, Instance instance) {
        return Math.abs(userData.getCtr()-(userData.getAbr()*instance.getFeature(0)+userData.getDpv()*instance.getFeature(1)));
    }

    @Override
    public Dimension getDim() {
        return dimensions;
    }
}
