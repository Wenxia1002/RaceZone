/**
 * Class continue
 *
 * @author Yi-Qi Hu
 * @time 2015.11.14
 * @version 2.0
 * continue class is the method of Racos in continuous feasible region
 */

package Racos.Method;

import MPC.Point;
import Racos.Tools.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import Racos.Componet.*;
import Racos.ObjectiveFunction.*;

public class Continue extends BaseParameters {

    private Task task;                   //the task that algorithm will optimize
    private Dimension dimension;         //the task dimension message that algorithm optimize
    private Instance[] Pop;              //the instance set that algorithm will handle with
    private Instance[] NextPop;          //the instance set that algorithm will generate using instance set Pop
    private Instance[] PosPop;           //the instance set with best objective function value
    private Instance Optimal;            //an instance with the best objective function value
    private boolean on_off;                 //switch of sequential racos
    public int BudCount;
    public double StandScore;
    public double maxDis;
    public File log;
    public Path path;
    private Model model;
    private RandomOperator ro;
    public int bestCount;
    public double prebest;
    int ChoosenPos;
    double GlobalSample;
    boolean reSample;
    boolean write = false;
    int param4each;

    public boolean checkstopflag(int i) {
        double bestValue = getOptimal().getValue();
        System.out.println("i=" + i + "  best Value=" + bestValue);
        if (i == MaxIteration - 1) {
            System.out.println("\ncurrent Iteration: " + i);
            return true;
        }
        if (i != 1) {
            if (bestValue < 0 && Math.abs(bestValue - prebest) < 0.00001) {
                bestCount++;
                if (bestCount > 30) { // larger convergence ratio ?
//					iterativeNums = i;
                    System.out.println("\ncurrent Iteration: " + i);
                    return true;
                }
            } else {
                bestCount = 0;
            }
        }
        prebest = bestValue;


        return false;

    }

    public boolean checkIns(int j, Instance instance) {
        NextPop[j] = instance;
        return !notExistInstance(j, NextPop[j]);
    }

    public double setValue(int j, String[][] val) {
        double[] v = calculateValue(NextPop[j], val);
        NextPop[j].setValue(v[3]);//query
        NextPop[j].setTotalpoint();
        NextPop[j].setInfeasiblepoint((int) v[1]);
        return v[3];
    }


    public double[] calculateValue(Instance ins, String[][] val) {
        int via_num = (int) ins.getFeature(0);
        double[] lastPoint = task.getStart().getX();
        double[] X = new double[3];
        double[] value = new double[4];//value[0]:0/1 value[1]:collision-pos value[2]:collision-depth value[3]:value
        value[0] = Double.parseDouble(val[6][0]);
        value[1] = Double.parseDouble(val[7][0]);
        value[2] = -1;
        if (val[8][0] != null)
            value[2] = Double.parseDouble(val[8][0]);

        //calculate the distance from start to target
        double dist = 0;
        ArrayList<Double> dis4each = new ArrayList<>();

        for (int i = 0; i < via_num; i++) {
            X[0] = ins.getFeature(1 + i * param4each);
            X[1] = ins.getFeature(1 + i * param4each + 1);
            X[2] = ins.getFeature(1 + i * param4each + 2);

            double tmpdist = getEucliDistance(lastPoint, X);//calculate the EuclideanDistance between last point and current point

            dist += tmpdist;
            dis4each.add(tmpdist);

            lastPoint = X.clone();
        }
        double tmpdist = getEucliDistance(lastPoint, task.getTarget().getX());

        dist += tmpdist;
        dis4each.add(tmpdist);
        value[3] = 0;
        if (value[0] == 0) {//do not crash
            value[3] = dist - maxDis; //The shorter the distance, the better the score.
        } else {//crash
            int frag = 0;
            double tmpval = value[1], len = 0;
            while (true) {
                if (tmpval <= 0) break;
                else {
                    if (tmpval >= 10)
                        len += dis4each.get(frag);
                    else len += tmpval / 10 * dis4each.get(frag);
                    tmpval -= 10;
                    frag++;
                }
            }
            value[3] = (1 - len / dist) * StandScore;
        }
        if (false) {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < 31; i++) {
                s.append(String.format("%.2f", ins.getFeature(i))).append("\t");
            }
            s.append(String.format("%.2f", val)).append("\t").append(String.format("%.2f", value)).append("\n");
            try {
                Files.write(path, s.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            } catch (IOException e) {
                System.out.println("Open logs.txt fail!");
            }
        }

        return value;
    }


    private double getEucliDistance(double[] lastPoint, double[] X) {
        double dis = 0;
        for (int i = 0; i < 3; i++) {
            dis += Math.pow(Math.abs(lastPoint[i] - X[i]), 2);
        }
        return Math.sqrt(dis);
    }

    public void copyNextPop() {
        //copy NextPop
        for (int j = 0; j < this.SampleSize; j++) {
            Pop[j] = NextPop[j];
        }
        UpdatePosPop();//update PosPop set
        UpdateOptimal();//obtain optimal
    }

    public String[][] getResult() {
        Instance ins = getOptimal();

        int via_num = (int) ins.getFeature(0);//the number of vialocations
        int index = 1;
        String[][] ans = new String[6][7];
        ans[0][0] = Integer.toString(via_num);
        for (int j = 0; j < via_num; j++) {
            ans[j + 1][0] = task.getId() + Integer.toString(j + 1);
            ans[j + 1][1] = Double.toString(ins.getFeature(index));
            ans[j + 1][2] = Double.toString(ins.getFeature(index + 1));
            ans[j + 1][3] = Double.toString(ins.getFeature(index + 2));
            ans[j + 1][4] = Double.toString(ins.getFeature(index + 3));
            ans[j + 1][5] = Double.toString(ins.getFeature(index + 4));
            ans[j + 1][6] = Double.toString(ins.getFeature(index + 5));
            index += 6;
        }

        return ans;
    }

    public String getId() {
        return task.getId();
    }

    public Point getStart() {
        return task.getStart();
    }


    private class Model {                 //the model of generating next instance

        public double[][] region;//shrinked region
        public boolean[] label;  //if label[i] is false, the corresponding dimension should be randomized from region

        public Model(int size) {
            region = new double[size][2];
            label = new boolean[size];
            for (int i = 0; i < size; i++) {
                region[i][0] = 0;
                region[i][1] = 1;
                label[i] = false;
            }
        }

        public void PrintLabel() {
            for (int i = 0; i < label.length; i++) {
                if (!label[i]) {
                    System.out.print(i + " ");
                }
            }
            System.out.println();
        }
    }

    /**
     * constructors function
     * user must construct class Continue with a class which implements interface Task
     *
     * @param ta the class which implements interface Task
     */
    public Continue(Task ta) {
        task = ta;
        dimension = ta.getDim();
        ro = new RandomOperator();
        this.on_off = false;        //set batch-racos
        bestCount = 0;
        prebest = Double.MAX_VALUE;
        StandScore = 1000;
        maxDis = 10000;

        if (write) {
            log = new File("logs.txt");
            if (log.exists()) log.delete();

            try {
                log.createNewFile();
            } catch (Exception e) {
                System.out.println(e);
            }

            path = log.toPath();
        }

    }

    //the next several functions are prepared for testing
    public void PrintPop() {
        System.out.println("Pop set:");
        if (Pop.length == 0) {
            System.out.println("Pop set is null!");
        }
        for (int i = 0; i < Pop.length; i++) {
            Pop[i].PrintInstance();
        }
    }

    public void PrintPosPop() {
        System.out.println("PosPop set:");
        if (PosPop.length == 0) {
            System.out.println("PosPop set is null!");
        }
        for (int i = 0; i < PosPop.length; i++) {
            PosPop[i].PrintInstance();
        }
    }

    public void PrintNextPop() {
        System.out.println("NextPop set:");
        if (NextPop.length == 0) {
            System.out.println("NextPop set is null!");
        }
        for (int i = 0; i < NextPop.length; i++) {
            NextPop[i].PrintInstance();
        }
    }

    public void PrintOptimal() {
        System.out.println("Optimal:");
        Optimal.PrintInstance();
    }

    public void PrintRegion() {
        System.out.println("Region:");
        for (int i = 0; i < model.region.length; i++) {
            System.out.print("[" + model.region[i][0] + "," + model.region[i][1] + "] ");
        }
        System.out.println();
    }

    /**
     * using sequential Racos
     */
    public void TurnOnSequentialRacos() {
        this.on_off = true;
        return;
    }

    /**
     * using batch-Racos
     */
    public void TurnOffSequentialRacos() {
        this.on_off = false;
        return;
    }

    /**
     * @return the optimal that algorithm found
     */
    public Instance getOptimal() {
        return Optimal;
    }

    /**
     * RandomInstance without parameter
     *
     * @return an Instance, each feature in this instance is a random number from original feasible region
     */
    public Instance RandomInstance() {
        Instance ins = new Instance(dimension);
        for (int i = 0; i < dimension.getSize(); i++) {

            if (dimension.getType(i)) {//if i-th dimension type is continue
                ins.setFeature(i, ro.getDouble(dimension.getRegion(i)[0], dimension.getRegion(i)[1]));
            } else {//if i-th dimension type is discrete
                ins.setFeature(i, ro.getInteger((int) dimension.getRegion(i)[0], (int) dimension.getRegion(i)[1]));
            }
        }
        return ins;
    }

    /**
     * Random instance with parameter
     *
     * @param pos, the positive instance that generate the model
     * @return an Instance, each feature in this instance is a random number from a feasible region named model
     */
    public Instance RandomInstance(Instance pos) {
//		System.out.println("RandomInstance");
        Instance ins = new Instance(dimension);
//		model.PrintLabel();
        while (true) {
            for (int i = 0; i < dimension.getSize(); i++) {

                if (dimension.getType(i)) {//if i-th dimension type is continue
                    if (model.label[i]) {//according to fixed dimension, valuate using corresponding value in pos
                        ins.setFeature(i, pos.getFeature(i));
                    } else {//according to not fixed dimension, random in region
//					System.out.println("["+model.region[i][0]+", "+model.region[i][1]+"]");
                        ins.setFeature(i, ro.getDouble(Math.max(pos.region[i][0], model.region[i][0]), Math.min(pos.region[i][1], model.region[i][1])));
                    }
                } else {//if i-th dimension type is discrete
                    if (model.label[i]) {//according to fixed dimension, valuate using corresponding value in pos
                        ins.setFeature(i, pos.getFeature(i));
                    } else {//according to not fixed dimension, random in region
//						if(pos.region[i][1] < model.region[i][1]) {
//							System.out.println("optimization");
//						}
                        int bound1 = Math.max((int) pos.region[i][0], (int) model.region[i][0]), bound2 = Math.min((int) pos.region[i][1], (int) model.region[i][1]);
                        if (bound1 > bound2) {
//							System.out.println("Dimsion is " + i);
//							System.out.println("pos region [" + pos.region[i][0] + "," + pos.region[i][1] + "]");
//							System.out.println("model region [" + model.region[i][0] + "," + model.region[i][1] + "]");
                            int tmp = ro.getInteger((int) model.region[i][0], (int) model.region[i][1]);
                            ins.setFeature(i, tmp);
                        } else {
                            int tmp = ro.getInteger(bound1, bound2);
                            ins.setFeature(i, tmp);
                        }
                    }
                }

            }
			/*double sum = 0;
			for(int j = 0;j < ((ObjectFunction)task).getPathLength();++j){
				sum += ins.getFeature(j);
			}
			if(sum <= automatas.get(0).cycle/automatas.get(0).delta)*/
            break;
        }
        return ins;
    }

    /**
     * initialize Pop, NextPop, PosPop and Optimal
     */
    @SuppressWarnings("unchecked")
    protected void Initialize() {

        Instance[] temp = new Instance[SampleSize + PositiveNum];

        Pop = new Instance[SampleSize];

        //sample Sample+PositiveNum instances and add them into temp
        for (int i = 0; i < SampleSize + PositiveNum; i++) {
            temp[i] = RandomInstance();
            double val = task.getValue(temp[i]);
            temp[i].setValue(val);
            if (this.on_off) {
                this.BudCount++;
            }
        }

        //sort Pop according to objective function value
        InstanceComparator comparator = new InstanceComparator();
        java.util.Arrays.sort(temp, comparator);
        java.util.Arrays.sort(temp, comparator);

        //initialize Optimal
        Optimal = temp[0].CopyInstance();

        //after sorting, the beginning several instances in temp are used for initializing PosPop
        PosPop = new Instance[PositiveNum];
        for (int i = 0; i < PositiveNum; i++) {
            PosPop[i] = temp[i];
            //((ObjectFunction)task).updateInstanceRegion(PosPop[i]);
        }

        Pop = new Instance[SampleSize];
        for (int i = 0; i < SampleSize; i++) {
            Pop[i] = temp[i + PositiveNum];
        }

        //initialize NextPop
        NextPop = new Instance[SampleSize];

        model = new Model(dimension.getSize());

        return;

    }

    public void sortInstance(Instance[] temp) {

        //sort Pop according to objective function value
        InstanceComparator comparator = new InstanceComparator();
        java.util.Arrays.sort(temp, comparator);

        //initialize Optimal
        Optimal = temp[0].CopyInstance();

        //after sorting, the beginning several instances in temp are used for initializing PosPop
        PosPop = new Instance[PositiveNum];
        for (int i = 0; i < PositiveNum; i++) {
            PosPop[i] = temp[i];
            //((ObjectFunction)task).updateInstanceRegion(PosPop[i]);
        }

        Pop = new Instance[SampleSize];
        for (int i = 0; i < SampleSize; i++) {
            Pop[i] = temp[i + PositiveNum];
        }

        //initialize NextPop
        NextPop = new Instance[SampleSize];

        model = new Model(dimension.getSize());

        return;

    }

    /**
     * reset sampling model
     *
     * @return the model with original feasible region and all label is true
     */
    protected void ResetModel() {
        for (int i = 0; i < dimension.getSize(); i++) {
            model.region[i][0] = dimension.getRegion(i)[0];
            model.region[i][1] = dimension.getRegion(i)[1];
            model.label[i] = false;
        }
        return;
    }

    /**
     * shrink model for instance pos
     *
     * @param pos
     */
    protected void ShrinkModel(Instance pos) {
//		System.out.println("ShrinkModel");
        int ChoosenDim;
        int ChoosenNeg;
        double tempBound;

        int ins_left = SampleSize;
        int c = 0;

//		int totalPoint=(int)pos.getFeature(0);
//		int infeaspoint=pos.getInfeasiblepoint();
//		int size=dimension.getSize();;
//		//select model dimension
//		if(pos.getValue()>0){
//			int seg=infeaspoint/10;
//			if(seg==totalPoint) size=1+seg*6;
//			else size=1+(seg+1)*6;
//		}

//		if(totalPoint==1) size=dimension.getSize();
//		else size=pos.getValue()>0?1+(infeaspoint/10+1)*6:dimension.getSize();

        while (ins_left > 0) {//generate the model
            if (c > 1000) {
                System.out.println("dead loop");
            }
            ++c;
            ChoosenDim = ro.getInteger(0, dimension.getSize() - 1);//choose a dimension randomly
            ChoosenNeg = ro.getInteger(0, this.SampleSize - 1);    //choose a negative instance randomly
            // shrink model
            if (pos.getFeature(ChoosenDim) < Pop[ChoosenNeg].getFeature(ChoosenDim)) {
                tempBound = ro.getDouble(pos.getFeature(ChoosenDim), Pop[ChoosenNeg].getFeature(ChoosenDim));
                if (tempBound < model.region[ChoosenDim][1]) {
                    model.region[ChoosenDim][1] = tempBound;
                    int i = 0;
                    while (i < ins_left) {
                        if (Pop[i].getFeature(ChoosenDim) >= tempBound) {
                            ins_left--;
                            Instance tempins = Pop[i];
                            Pop[i] = Pop[ins_left];
                            Pop[ins_left] = tempins;
                        } else {
                            i++;
                        }
                    }

                }
            } else {
                tempBound = ro.getDouble(Pop[ChoosenNeg].getFeature(ChoosenDim), pos.getFeature(ChoosenDim));
                if (tempBound > model.region[ChoosenDim][0]) {
                    model.region[ChoosenDim][0] = tempBound;
                    int i = 0;
                    while (i < ins_left) {
                        if (Pop[i].getFeature(ChoosenDim) <= tempBound) {
                            ins_left--;
                            Instance tempins = Pop[i];
                            Pop[i] = Pop[ins_left];
                            Pop[ins_left] = tempins;
                        } else {
                            i++;
                        }
                    }
                }
            }
        }
        return;

    }

    /**
     * make sure that the number of random dimension is smaller than the threshold UncertainBits
     *
     * @return
     */
    protected void setRandomBitsByReturnRes(Instance pos) {
//		System.out.println("setRandomBits");
        int labelMarkNum;
        int[] labelMark = new int[dimension.getSize()];
        int tempLab;
        labelMarkNum = dimension.getSize();
        for (int k = 0; k < dimension.getSize(); k++) {
            labelMark[k] = k;
            model.label[k] = true;
        }

        int totalPoint = (int) pos.getFeature(0);
        int infeaspoint = pos.getInfeasiblepoint();
        int size = dimension.getSize();
        ;
        //select model dimension
//		if(pos.getValue()>0){
//			int seg=infeaspoint/10;
//			if(seg==totalPoint) size=1+seg*6;
//			else size=1+(seg+1)*6;
//		}
        for (int k = 0; k < UncertainBits; k++) {
            tempLab = ro.getInteger(0, size - 1);
            model.label[tempLab] = false;
        }

//		for (int k = 0; k < dimension.getSize()-UncertainBits-1; k++) {
//			tempLab = ro.getInteger(0, labelMarkNum-1);
//			model.label[labelMark[tempLab]] = true;
//			labelMark[tempLab] = labelMark[labelMarkNum-1];
//			labelMarkNum--;
//		}
        //model.label[0]=false;
        return;

    }

    /**
     * make sure that the number of random dimension is smaller than the threshold UncertainBits
     *
     * @return
     */
    protected void setRandomBits() {
//		System.out.println("setRandomBits");
        int labelMarkNum;
        int[] labelMark = new int[dimension.getSize()];
        int tempLab;
        labelMarkNum = dimension.getSize();
        for (int k = 0; k < dimension.getSize(); k++) {
            labelMark[k] = k;
        }
        for (int k = 0; k < dimension.getSize() - UncertainBits - 1; k++) {
            tempLab = ro.getInteger(0, labelMarkNum - 1);
            model.label[labelMark[tempLab]] = true;
            labelMark[tempLab] = labelMark[labelMarkNum - 1];
            labelMarkNum--;
        }
        //model.label[0]=false;
        return;

    }

    /**
     * if each instance in Pop is not in model, return true; if exist more than one instance in Pop is in the model, return false
     *
     * @return true or false
     */
    protected boolean Distinguish() {
        int j;
        for (int i = 0; i < this.SampleSize; i++) {
            for (j = 0; j < dimension.getSize(); j++) {
                if (Pop[i].getFeature(j) > model.region[j][0] && Pop[i].getFeature(j) < model.region[j][1]) {

                } else {
                    break;
                }
            }
            if (j == dimension.getSize()) {
                return false;
            }
        }
        return true;
    }

    /**
     * judge whether the instance is in the region of model, if instance is in the model, return true, else return true
     *
     * @param model, the model
     * @param ins,   the instance
     * @return
     */
    protected boolean InstanceIsInModel(Model model, Instance ins) {
        for (int i = 0; i < ins.getFeature().length; i++) {
            if (ins.getFeature(i) > model.region[i][1] || ins.getFeature(i) < model.region[i][0]) {
                return false;//if i-th dimension is not in the region, return false
            }
        }
        return true;
    }

    /**
     * if ins exist in Pop, PosPop, NextPop, return true; else return false
     *
     * @param ins
     * @return
     */
    protected boolean notExistInstance(int end, Instance ins) {
//		System.out.println("notExistInstance");
        int i, j;
        for (i = 0; i < this.PositiveNum; i++) {
            if (ins.Equal(PosPop[i])) {
                return false;
            }
        }
        for (i = 0; i < this.SampleSize; i++) {
            if (ins.Equal(Pop[i])) {
                return false;
            }
        }
        for (i = 0; i < end; i++) {
            if (ins.Equal(NextPop[i])) {
                return false;
            }
        }
        return true;

    }

    /**
     * update set PosPop using NextPop
     */
    protected void UpdatePosPop() {
//		System.out.println("UpdatePosPop");
        Instance TempIns = new Instance(dimension);
        int j;
        for (int i = 0; i < this.SampleSize; i++) {
            for (j = 0; j < this.PositiveNum; j++) {
                if (PosPop[j].getValue() > Pop[i].getValue()) {
                    break;
                }
            }
            if (j < this.PositiveNum) {
                //((Mission)task).updateInstanceRegion(Pop[i]);
                TempIns = Pop[i];
                Pop[i] = PosPop[this.PositiveNum - 1];
                for (int k = this.PositiveNum - 1; k > j; k--) {
                    PosPop[k] = PosPop[k - 1];
                }
                PosPop[j] = TempIns;
            }
        }
        return;
    }

    /**
     * update sample set for sequential racos
     */
    protected void UpdateSampleSet(Instance temp) {
        Instance TempIns = new Instance(dimension);
        RandomOperator ro = new RandomOperator();
        int j;

        for (j = 0; j < this.PositiveNum; j++) {
            if (PosPop[j].getValue() > temp.getValue()) {
                break;
            }
        }
        if (j < this.PositiveNum) {
            TempIns = temp;
            temp = PosPop[this.PositiveNum - 1];
            for (int k = this.PositiveNum - 1; k > j; k--) {
                PosPop[k] = PosPop[k - 1];
            }
            PosPop[j] = TempIns;
        }

        for (j = 0; j < this.SampleSize; j++) {
            if (Pop[j].getValue() > temp.getValue())
                break;
        }
        if (j < this.SampleSize) {
            for (int k = this.SampleSize - 1; k > j; k--) {
                Pop[k] = Pop[k - 1];
            }
            Pop[j] = temp;
        }
        return;
    }


    /**
     * update optimal
     */
    protected void UpdateOptimal() {
        if (Optimal.getValue() > PosPop[0].getValue()) {
            Optimal = PosPop[0];
        }
        return;
    }

    public void prepare(int num) {
        this.BudCount = 0;
        model = new Model(dimension.getSize());
        param4each = num;
        ResetModel();
    }


    public Instance getPos() {
        ResetModel();
        ChoosenPos = ro.getInteger(0, this.PositiveNum - 1);
        GlobalSample = ro.getDouble(0, 1);
        if (GlobalSample >= this.RandProbability) {
        } else {
            ShrinkModel(PosPop[ChoosenPos]);//shrinking model
            setRandomBitsByReturnRes(PosPop[ChoosenPos]);//set uncertain bits
        }

        return PosPop[ChoosenPos];
    }

    /**
     * after setting parameters of Racos, user call this function can obtain optimal
     */
    public ValueArc run() {

        this.BudCount = 0;

        /////////////////////////////////////
        double[] result = new double[6];

        model = new Model(dimension.getSize());

        ResetModel();
        Initialize();
//		HashSet<Double> pathchoice1=new HashSet<>();
//		HashSet<Double> pathchoice2=new HashSet<>();
//		for(int i=0; i<PositiveNum;i++){
//			pathchoice1.add(PosPop[i].getFeature(0));
//			pathchoice2.add(PosPop[i].getFeature(1));
//		}
//		for(int i=0; i<SampleSize;i++){
//			pathchoice1.add(Pop[i].getFeature(0));
//			pathchoice2.add(Pop[i].getFeature(1));
//		}




        int bestValueCount = 0;
        int sumCount = 0;
        int iterativeNums = this.MaxIteration;
        ArrayList<Double> arrayListBestValues = new ArrayList<>();
        // batch Racos
        if (!this.on_off) {
            double preBestValue = Double.MAX_VALUE;
            // for each loop
            for (int i = 1; i < MaxIteration; i++) {
                double bestValue = getOptimal().getValue();
                System.out.println("i=" + i + "  best Value=" + bestValue);
                if (bestValue <= 0 || i == MaxIteration - 1) {
                    task.getValue(Optimal);
                    System.out.println("\ncurrent Iteration: " + i);
                    break;
                }

//				System.out.println(i + " " + bestValue);
                if (i != 1) {
                    if (Math.abs(bestValue - preBestValue) < 0.00001) {
                        bestValueCount++;
                        if (bestValueCount > 30) { // larger convergence ratio ?
                            iterativeNums = i;
                            task.getValue(Optimal);
                            System.out.println("\ncurrent Iteration: " + i);
                            break;
                        }
                    } else {
                        bestValueCount = 0;
                    }
                }
                preBestValue = bestValue;
                // for each sample in a loop
                for (int j = 0; j < this.SampleSize; j++) {
                    reSample = true;
                    while (reSample) {
                        ResetModel();
                        ChoosenPos = ro.getInteger(0, this.PositiveNum - 1);
                        GlobalSample = ro.getDouble(0, 1);
                        if (GlobalSample >= this.RandProbability) {
                        } else {
                            ShrinkModel(PosPop[ChoosenPos]);//shrinking model
                            setRandomBits();//set uncertain bits
                        }
                        NextPop[j] = RandomInstance(PosPop[ChoosenPos]);//

                        if (notExistInstance(j, NextPop[j])) {
                            double val = task.getValue(NextPop[j]);
                            NextPop[j].setValue(val);//query

                            reSample = false;
                        }
                    }
//					if(k == 100){
//						System.out.println("reset reSample");
//						NextPop[j].setValue(task.getValue(NextPop[j]));
//					}
                }
                //copy NextPop
                for (int j = 0; j < this.SampleSize; j++) {
                    Pop[j] = NextPop[j];
                }
                UpdatePosPop();//update PosPop set
                UpdateOptimal();//obtain optimal
            }
        } else {
            // sequential Racos
            Instance new_sample = null;
            for (; BudCount < this.Budget; ) {
                reSample = true;
                while (reSample) {
                    ResetModel();
                    ChoosenPos = ro.getInteger(0, this.PositiveNum - 1);
                    GlobalSample = ro.getDouble(0, 1);
                    if (GlobalSample >= this.RandProbability) {
                    } else {
                        //ShrinkModel(PosPop[ChoosenPos]);//shrinking model
                        setRandomBits();//set uncertain bits
                    }
                    new_sample = RandomInstance(PosPop[ChoosenPos]);//sample
                    if (notExistInstance(0, new_sample)) {
                        new_sample.setValue(task.getValue(new_sample));//query
                        BudCount++;
                        reSample = false;
                    }
                }
                UpdateSampleSet(new_sample);//update PosPop set
                UpdateOptimal();//obtain optimal

            }
        }

		/*System.out.println("\nArm1 tries "+pathchoice1.size()+"/39 different paths.");
		System.out.println("Arm2 tries "+pathchoice2.size()+"/39 different paths.");
*/
        Weld of = (Weld) task;
        of.valueArc.iterativeNums = iterativeNums;
        of.valueArc.arrayListBestValues = arrayListBestValues;
        return of.valueArc;
    }

    public ValueArc CEM() {
        int max_iter = 100;
        int sample_size = 40;
        int elite_size = 10;
        double[] gaussion_mean = init_mean();
        double[] gaussion_variance = init_variance();

        for (int i = 0; i < max_iter; ++i) {
            Instance[] instances = gaussionSampling(sample_size, gaussion_mean, gaussion_variance);
            for (int j = 0; j < sample_size; ++j) {
                instances[j].setValue(task.getValue(instances[j]));
            }
            java.util.Arrays.sort(instances, new InstanceComparator());

            if (i == 0 || Optimal.getValue() > instances[0].getValue())
                Optimal = instances[0];
            System.out.println("iteration" + i + " best value :" + task.getValue(Optimal));

            //update mean and std
            for (int j = 0; j < dimension.getSize(); ++j) {
                double mean_j = 0.0, variance_j = 0.0;
                for (int k = 0; k < elite_size; ++k) {
                    mean_j += instances[k].getFeature(j);
                }
                mean_j /= elite_size;
                for (int k = 0; k < elite_size; ++k) {
                    variance_j += Math.pow(instances[k].getFeature(j) - mean_j, 2);
                }
                variance_j /= (elite_size - 1);
                gaussion_mean[j] = mean_j;
                gaussion_variance[j] = variance_j;
            }
        }
        Weld of = (Weld) task;
        return of.valueArc;
    }

    public Instance[] gaussionSampling(int sample_size, double[] mean, double[] variance) {
        Instance[] instances = new Instance[sample_size];
        for (int i = 0; i < sample_size; ++i) {
            instances[i] = new Instance(dimension);
            for (int j = 0; j < dimension.getSize(); ++j) {
                double t = ro.getGaussion(mean[j], variance[j]);
                while (t < dimension.getRegion(j)[0] || t > dimension.getRegion(j)[1]) {
                    t = ro.getGaussion(mean[j], variance[j]);
                }
                instances[i].setFeature(j, t);
            }
        }
        return instances;
    }

    public double[] init_mean() {
        double[] mean = new double[dimension.getSize()];
        for (int i = 0; i < dimension.getSize(); ++i) {
            mean[i] = (dimension.getRegion(i)[0] + dimension.getRegion(i)[1]) / 2.0;
        }
        return mean;
    }

    public double[] init_variance() {
        double[] variance = new double[dimension.getSize()];
        for (int i = 0; i < dimension.getSize(); ++i)
            variance[i] = Math.pow((dimension.getRegion(i)[1] - dimension.getRegion(i)[0]), 2); // may be too large for narrow range
        return variance;
    }

    public ValueArc GA() {
        double mutationProb = 0.3; // the probability of mutation
        int max_iter = 20;
        int sample_size = 150;
        int eliteNum = 20;
        Instance[] instances = new Instance[sample_size];
        for (int i = 0; i < sample_size; ++i) instances[i] = RandomInstance();

        for (int i = 0; i < max_iter; ++i) {
            for (int j = 0; j < sample_size; ++j) instances[j].setValue(task.getValue(instances[j]));

            java.util.Arrays.sort(instances, new InstanceComparator());

            if (i == 0 || Optimal.getValue() > instances[0].getValue())
                Optimal = instances[0];

            System.out.println("iteration" + i + " best value :" + Optimal.getValue());

            instances = crossover(instances, eliteNum, sample_size);

            mutation(instances, mutationProb);
        }
        Weld of = (Weld) task;
        return of.valueArc;
    }

    public Instance[] crossover(Instance[] instances, int eliteNum, int sample_size) {
        int currentPos = 0;
        Instance[] newInstances = new Instance[sample_size];
        for (int i = 0; i < eliteNum; ++i) {
            for (int j = i + 1; j < eliteNum; ++j) {
                newInstances[currentPos] = crossoverBetweenTwoIns(instances[i], instances[j]);
                if (++currentPos >= sample_size) {
                    i = eliteNum;
                    break;
                }
            }
        }
        for (int i = 0; i < sample_size && currentPos < sample_size; ++i, ++currentPos)
            newInstances[currentPos] = instances[i];
        return newInstances;
    }

    public void mutation(Instance[] instances, double mutationProb) {
        int instancePosition = (int) (Math.random() * instances.length);
        for (int i = 0; i < dimension.getSize(); ++i) {
            if (Math.random() > mutationProb) continue;
            int mutationPos = (int) (Math.random() * 8);
            byte[] bytes = doubleToBytes(instances[instancePosition].getFeature(i));
            bytes[mutationPos] = (byte) (~(long) (bytes[mutationPos] & 0xff));
        }
    }

    public Instance crossoverBetweenTwoIns(Instance ins_i, Instance ins_j) {
        Instance new_ins = new Instance(dimension.getSize());
        for (int i = 0; i < dimension.getSize(); ++i) {
            byte[] arr_i = doubleToBytes(ins_i.getFeature(i));
            byte[] arr_j = doubleToBytes(ins_j.getFeature(i));
            int crossPos = (int) (Math.random() * 7);
            for (int j = crossPos + 1; j < 8; ++j)
                arr_i[j] = arr_j[j];
            new_ins.setFeature(i, bytesToDouble(arr_i));
        }
        return new_ins;
    }

    public byte[] doubleToBytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; ++i) {
            bytes[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return bytes;
    }

    public double bytesToDouble(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; ++i) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

    public ValueArc RRT() {
        int max_iter = 25;
        int sample_size = 100;
        int elite_size = 20;

        Instance[] instances = new Instance[sample_size];

        for (int i = 0; i < sample_size; ++i) instances[i] = RandomInstance();

        for (int i = 0; i < max_iter; ++i) {
            for (int k = 0; k < sample_size; ++k) instances[k].setValue(task.getValue(instances[k]));

            java.util.Arrays.sort(instances, new InstanceComparator());

            if (i == 0 || Optimal.getValue() > instances[0].getValue()) Optimal = instances[0];

            System.out.println("iteration" + i + " best value :" + Optimal.getValue());

            instances = extend(instances, elite_size, sample_size);

//			for(int j = elite_size;j < sample_size;++j) instances[j] = RandomInstance();

        }

        Weld of = (Weld) task;
        return of.valueArc;
    }

    public Instance[] extend(Instance[] instances, int elite_size, int sample_size) {
        int extend_num = 50;
        for (int i = 0; i < extend_num; ++i) {
            int chosenPos = ro.getInteger(elite_size, sample_size - 1);
            int chosenDimension = ro.getInteger(0, dimension.getSize() - 1);
            double delta = ro.getDouble(-0.5, 0.5);
            if (!dimension.getType(chosenDimension)) { // discrete
                if (delta <= 0) delta = -1;
                else delta = 1;

            }
            double newValue = instances[chosenPos].getFeature(chosenDimension) + delta;
            if (newValue >= dimension.getRegion(chosenDimension)[0] && newValue <= dimension.getRegion(chosenDimension)[1]) {
                instances[chosenPos].setFeature(chosenDimension, newValue);
//				System.out.println("success");
            }
        }
        return instances;
    }

    public ValueArc monte() {
        int max_iter = 50;
        int sample_size = 100;
        int elite_size = 10;

        Instance[] instances = new Instance[sample_size];

        for (int i = 0; i < sample_size; ++i) instances[i] = RandomInstance();

        for (int i = 0; i < max_iter; ++i) {
            for (int k = 0; k < sample_size; ++k) instances[k].setValue(task.getValue(instances[k]));

            java.util.Arrays.sort(instances, new InstanceComparator());

            if (i == 0 || Optimal.getValue() > instances[0].getValue()) Optimal = instances[0];

            System.out.println("iteration" + i + " best value :" + Optimal.getValue());

            instances = rejectSampling(instances, elite_size, sample_size);
        }

        Weld of = (Weld) task;
        return of.valueArc;
    }

    public Instance[] rejectSampling(Instance[] instances, int elite_size, int sample_size) {
        int sampling_num = 50;
        for (int i = 0; i < sampling_num; ++i) {
            int chosenPos = ro.getInteger(elite_size, sample_size - 1);
            int chosenDimension = ro.getInteger(0, dimension.getSize() - 1);
            double max_v = dimension.getRegion(chosenDimension)[0], min_v = dimension.getRegion(chosenDimension)[1];
            for (int j = 0; j < elite_size; ++j) {
                max_v = Math.max(max_v, instances[j].getFeature(chosenDimension));
                min_v = Math.min(min_v, instances[j].getFeature(chosenDimension));
            }
            max_v = Math.min(max_v + 0.5, dimension.getRegion(chosenDimension)[1]);
            min_v = Math.max(min_v - 0.5, dimension.getRegion(chosenDimension)[0]);

            instances[chosenPos].setFeature(chosenDimension, ro.getDouble(min_v, max_v));
        }
        return instances;
    }

}
