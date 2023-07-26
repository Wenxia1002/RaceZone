package MPC;

import Racos.Componet.Instance;
import Racos.Method.Continue;
import Racos.ObjectiveFunction.RaceZone;
import Racos.ObjectiveFunction.Task;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Model {

    private final Continue con;
    private final int bound = 5;
    private final int samplesize = 5;
    private final int iteration = 2000;
    private int budget = 4000;
    private final int positivenum = 2;
    private double probability = 0.75;
    private int uncertainBit = 2;

    public Model(String trainDataFile, int size) {
        ArrayList<UserData> userDataList = processData(trainDataFile);
        Task t = new RaceZone(userDataList, size);
        con = new Continue(t);
        con.setMaxIteration(iteration);
        con.setSampleSize(samplesize);
        con.setBudget(budget);
        con.setPositiveNum(positivenum);
        con.setRandProbability(probability);
        con.setUncertainBits(uncertainBit);
        con.prepare();

        con.run();

        Instance opt=con.getOptimal();
        System.out.println(opt.getFeature(0));
        System.out.println(opt.getFeature(1));

    }

    private ArrayList<UserData> processData(String trainDataFile) {
        BufferedReader reader=null;

        try {

            reader = new BufferedReader(new FileReader(trainDataFile));
            String DELIMITER = ",";
            String line;
            double value = 0;
            int index = -1;
            ArrayList<UserData> userDataArrayList=new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                index++;
                if(index==0) {
                    continue;
                }
                String[] columns = line.split(DELIMITER);
                UserData userData=new UserData(columns[0],columns[1],columns[2],columns[3]);
                userDataArrayList.add(userData);
            }

            return userDataArrayList;
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("IO Exception" + '\n' + e.getMessage());
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {

        String trainDataFile = "test.csv";
        int featureSize = 2;

        //model initialize
        Model model = new Model(trainDataFile, featureSize);

        ////Initial sampling
        //String[][][] groupIns=model.getInitialInstances();
        //
        ////Evaluate all instances in groupIns
        //String[][][] evalRes=Evaluate(groupIns);
        ////Sort instances by the evaluated result
        //double[] scores=model.sortIns(evalRes);
        //
        //
        ////Repeat sampling and evaluate
        //for(int i=1; i<model.getIteration(); i++){
        //    boolean stop=model.checkstop(i);
        //    if(stop) {
        //        break;
        //    }
        //    groupIns=model.sample();
        //    evalRes=Evaluate(groupIns);
        //    scores=model.update(evalRes);
        //
        //}



    }

}
