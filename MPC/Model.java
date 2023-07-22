package MPC;

import Racos.Componet.Instance;
import Racos.Method.Continue;
import Racos.ObjectiveFunction.Task;
import Racos.ObjectiveFunction.Weld;

import java.util.HashMap;
import java.util.Random;

public class Model {

    private Task t;
    private Continue con;
    private int bound=5;                // parameter: the number of vialocations
    private int samplesize = 5;       // parameter: the number of samples in each iteration
    private int iteration = 800;       // parameter: the number of iterations for batch racos
    private int budget = 4000;         // parameter: the budget of sampling for sequential racos
    private int positivenum = 2;       // parameter: the number of positive instances in each iteration
    private double probability = 0.85; // parameter: the probability of sampling from the model
    private int uncertainbit = 1;      // parameter: the number of sampled dimensions
    private double infeasible=10000;
    private int sampleparam4each =3;
    private int recordparam4each=6;

//    private HashMap<String, Double> xyzValue;
//    private HashMap<String,String> allParams;
    public String bestIns;
    public Double bestValue;

    private Instance[] temp;

    public Model(String[] start,String[] target){
        t = new Weld(start,target,bound);

        con=new Continue(t);
        con.setMaxIteration(iteration);
        con.setSampleSize(samplesize);      // parameter: the number of samples in each iteration
        con.setBudget(budget);              // parameter: the budget of sampling
        con.setPositiveNum(positivenum);    // parameter: the number of positive instances in each iteration
        con.setRandProbability(probability);// parameter: the probability of sampling from the model
        con.setUncertainBits(uncertainbit); // parameter: the number of samplable dimensions
        con.prepare(sampleparam4each);

//        xyzValue=new HashMap<>();
//        allParams=new HashMap<>();

        bestIns=null;
        bestValue=null;

    }

    public static void main(String[] args) {

        String[] start=new String[]{"start","12","23","34","1.2","1.4","0.7"};//start Point
        String[] target=new String[]{"end","112","213","314","0.2","0.4","1.7"};//target Point

        //model initialize
        Model model=new Model(start,target);

        //Initial sampling
        String[][][] groupIns=model.getInitialInstances();

        //Evaluate all instances in groupIns
        String[][][] evalRes=Evaluate(groupIns);
        //Sort instances by the evaluated result
        double[] scores=model.sortIns(evalRes);
        String optimalstr= model.bestIns;



        //Repeat sampling and evaluate
        for(int i=1; i<model.getIteration(); i++){
            boolean stop=model.checkstop(i);//check whether to stop the algorithm
            if(stop) break;
            groupIns=model.sample();//Sampling
            evalRes=Evaluate(groupIns);//Evaluate
            scores=model.update(evalRes);//Refine model
            optimalstr= model.bestIns;
        }
        //Get optimal control result
        optimalstr= model.bestIns;
        String[][] optimal=model.getOptimalIns();
        double best_score=model.getOptimalScore();
    }

    public double getOptimalScore() {
        return bestValue;
    }

    public String[][] ins2String(Instance ins){
        String[][] data=new String[6][7];
        int via_num= (int) ins.getFeature(0);
        data[0][0]=String.valueOf(via_num);
        int index=1;
        for(int i=1;i<=via_num;i++){
            data[i][0]=con.getId()+Integer.toString(i);
            data[i][1]=Double.toString(ins.getFeature(index));
            data[i][2]=Double.toString(ins.getFeature(index+1));
            data[i][3]=Double.toString(ins.getFeature(index+2));
            data[i][4]=Double.toString(ins.getFeature(index+3));
            data[i][5]=Double.toString(ins.getFeature(index+4));
            data[i][6]=Double.toString(ins.getFeature(index+5));
            index+=6;
        }

        return data;
    }

    public String[][] getOptimalIns() {
        return recordInsInfo(bestIns);
    }

    private static String[][][] Evaluate(String[][][] groupIns) {
        double[] result=new double[]{1,2,3,4,5,6,7,8,9};
        Random ra=new Random();
        int len=groupIns.length;
        String[][][] ans=new String[len][9][6];
        for(int i=0;i<len;i++){
            ans[i][0][0]= groupIns[i][0][0];
            for(int k=1;k<Integer.parseInt(ans[i][0][0])+1;k++){
                ans[i][k][0]= groupIns[i][k][0];
                for(int j=0;j<3;j++){
                    ans[i][k][j]= groupIns[i][k][j+1];
                }
                for(int j=3;j<6;j++){
                    ans[i][k][j]= String.valueOf(ra.nextDouble());
                }
            }
            ans[i][6][0]= String.valueOf(ra.nextInt(2));
            ans[i][7][0]= String.valueOf(ra.nextInt((Integer.parseInt(groupIns[i][0][0]))+1)*10);
            ans[i][8][0]= null;
        }

        return ans;
    }

    public double[] update(String[][][] evalRes) {
        double[] scores=new double[samplesize];
        for(int i=0; i<samplesize; i++){
            scores[i]=con.setValue(i,evalRes[i]);
            record(evalRes[i],scores[i]);
        }
        con.copyNextPop();
        return scores;
    }

    public double[] sortIns(String[][][] evalRes) {
        double[] scores=new double[samplesize+positivenum];
        for(int i=0; i<samplesize+positivenum; i++){
            double[] val=con.calculateValue(temp[i],evalRes[i]);
            record(evalRes[i],val[3]);
            temp[i].setValue(val[3]);
            temp[i].setInfeasiblepoint((int)val[1]);
            temp[i].setTotalpoint();
            scores[i]=val[3];
        }
        con.sortInstance(temp);
        return scores;
    }

    private void record(String[][] evalRes, double val) {
//        String temp=eva2Str(evalRes,sampleparam4each);

//        if(!xyzValue.containsKey(temp) || xyzValue.get(temp) > val){
//            xyzValue.put(temp, val);
//        if(!allParams.containsKey(temp)){
//            allParams.put(temp, eva2Str(evalRes, recordparam4each));
//        }
        if(bestValue == null||bestValue>val){
            bestValue=val;
            bestIns=eva2Str(evalRes,recordparam4each);
        }
    }

    private String eva2Str(String[][] evalRes, int paramsize) {
        StringBuilder tmp=new StringBuilder();
        int via_num=Integer.parseInt(evalRes[0][0]);
        tmp.append(via_num).append(",");//via_num,
        for(int i=0;i<via_num;i++){
            for(int j=0;j<paramsize;j++){
                tmp.append(evalRes[i + 1][j]).append(",");
            }
        }
        return tmp.toString();
    }


//    public double readRes(String[][] result) {
//        double crash_or_not=Double.parseDouble(result[6][0]);
//        if(crash_or_not==1){//crash
//            return Double.parseDouble(result[6][0])+1;
//        }else return 0;
//    }

    public String[][][] sample() {
        String[][][] groupIns=new String[samplesize][6][7];
        temp=new Instance[samplesize];
        // sample and evaluate
        for(int i=0; i<samplesize; i++){
            boolean reSample = true;
            while (reSample) {
                Instance posIns=con.getPos();
                Instance instance=con.RandomInstance(posIns);
                reSample=con.checkIns(i,instance);
                if(!reSample){
                    temp[i]=instance;
                    groupIns[i]= recordInsInfo(temp[i]);

                }
            }
        }

        return groupIns;
    }

    public String[][] recordInsInfo(String best){
        String[][] insStr=new String[1+bound][7];
        String[] tmp=best.split(",");
        insStr[0][0]=tmp[0];
        int via_num= Integer.parseInt(tmp[0]);
        for(int j=0;j<via_num;j++){
            insStr[j+1][0]=con.getId()+ (j + 1);

            for(int k=0;k<sampleparam4each;k++){
                double pos=Double.parseDouble(tmp[1+k+j*recordparam4each]);
                insStr[j+1][k+1]=Double.toString(pos);
            }
            for(int k=3;k<recordparam4each;k++){
                double pos=Double.parseDouble(tmp[1+k+j*recordparam4each]);
                insStr[j+1][k+1]=Double.toString(pos);
            }
        }
        return insStr;

    }
    public String[][] recordInsInfo(Instance instance) {
        String[][] insStr=new String[1+bound][7];
        int index=1;
        insStr[0][0]= String.valueOf((int)instance.getFeature(0));
        int via_num= (int) instance.getFeature(0);
        for(int j=0;j<via_num;j++){
            insStr[j+1][0]=con.getId()+ (j + 1);
            for(int k=0;k<3;k++){
                double pos=instance.getFeature(index+k);
                insStr[j+1][k+1]=Double.toString(pos);
            }
            index+= sampleparam4each;
        }
        return insStr;
    }


    public boolean checkstop(int i) {
        return con.checkstopflag(i);
    }

    public int getIteration() {
        return iteration;
    }


    public String[][][] getInitialInstances() {
        String[][][] groupIns=new String[samplesize+positivenum][6][7];
        temp = new Instance[samplesize+positivenum];
        for(int i=0; i<samplesize+positivenum; i++){
            temp[i] = con.RandomInstance();
            groupIns[i]=recordInsInfo(temp[i]);
        }
        return groupIns;
    }
}
