package Racos.Method;

import Racos.Tools.*;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import Racos.Componet.*;
import Racos.ObjectiveFunction.*;

public class Continue extends BaseParameters {

    private Task task;
    private Dimension dimension;
    private Instance[] Pop;
    private Instance[] NextPop;
    private Instance[] PosPop;
    private Instance Optimal;

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

    public void copyNextPop() {
        //copy NextPop
        for (int j = 0; j < this.sampleSize; j++) {
            Pop[j] = NextPop[j];
        }
        UpdatePosPop();//update PosPop set
        UpdateOptimal();//obtain optimal
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
     * @param task the class which implements interface Task
     */
    public Continue(Task task) {
        this.task = task;
        dimension = task.getDim();
        ro = new RandomOperator();
        bestCount = 0;
        prebest = Double.MAX_VALUE;
        StandScore = 1000;
        maxDis = 10000;

        if (write) {
            log = new File("logs.txt");
            if (log.exists()) {log.delete();}

            try {
                log.createNewFile();
            } catch (Exception e) {
                System.out.println(e);
            }

            path = log.toPath();
        }

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
                ins.setFeature(i, ro.getInteger((int)dimension.getRegion(i)[0], (int)dimension.getRegion(i)[1]));
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
                        ins.setFeature(i, ro.getDouble(Math.max(pos.region[i][0], model.region[i][0]),
                            Math.min(pos.region[i][1], model.region[i][1])));
                    }
                } else {//if i-th dimension type is discrete
                    if (model.label[i]) {//according to fixed dimension, valuate using corresponding value in pos
                        ins.setFeature(i, pos.getFeature(i));
                    } else {//according to not fixed dimension, random in region
                        //						if(pos.region[i][1] < model.region[i][1]) {
                        //							System.out.println("optimization");
                        //						}
                        int bound1 = Math.max((int)pos.region[i][0], (int)model.region[i][0]), bound2 = Math.min(
                            (int)pos.region[i][1], (int)model.region[i][1]);
                        if (bound1 > bound2) {
                            //							System.out.println("Dimsion is " + i);
                            //							System.out.println("pos region [" + pos.region[i][0] + "," +
                            //							pos.region[i][1] + "]");
                            //							System.out.println("model region [" + model.region[i][0] + ","
                            //							+ model.region[i][1] + "]");
                            int tmp = ro.getInteger((int)model.region[i][0], (int)model.region[i][1]);
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

        Instance[] temp = new Instance[sampleSize + positiveNum];

        Pop = new Instance[sampleSize];

        //sample Sample+PositiveNum instances and add them into temp
        for (int i = 0; i < sampleSize + positiveNum; i++) {
            temp[i] = RandomInstance();
            double val = task.getValue(temp[i]);
            temp[i].setValue(val);

        }

        //sort Pop according to objective function value
        InstanceComparator comparator = new InstanceComparator();
        java.util.Arrays.sort(temp, comparator);
        java.util.Arrays.sort(temp, comparator);

        //initialize Optimal
        Optimal = temp[0].CopyInstance();

        //after sorting, the beginning several instances in temp are used for initializing PosPop
        PosPop = new Instance[positiveNum];
        for (int i = 0; i < positiveNum; i++) {
            PosPop[i] = temp[i];
            //((ObjectFunction)task).updateInstanceRegion(PosPop[i]);
        }

        Pop = new Instance[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            Pop[i] = temp[i + positiveNum];
        }

        //initialize NextPop
        NextPop = new Instance[sampleSize];

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
        PosPop = new Instance[positiveNum];
        for (int i = 0; i < positiveNum; i++) {
            PosPop[i] = temp[i];
            //((ObjectFunction)task).updateInstanceRegion(PosPop[i]);
        }

        Pop = new Instance[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            Pop[i] = temp[i + positiveNum];
        }

        //initialize NextPop
        NextPop = new Instance[sampleSize];

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
        int choosenDim;
        int choosenNeg;
        double tempBound;

        int insLeft = sampleSize;
        int c = 0;

        while (insLeft > 0) {//generate the model
            if (c > 1000) {
                System.out.println("dead loop");
            }
            ++c;
            choosenDim = ro.getInteger(0, dimension.getSize() - 1);//choose a dimension randomly
            choosenNeg = ro.getInteger(0, this.sampleSize - 1);    //choose a negative instance randomly
            // shrink model
            if (pos.getFeature(choosenDim) < Pop[choosenNeg].getFeature(choosenDim)) {
                tempBound = ro.getDouble(pos.getFeature(choosenDim), Pop[choosenNeg].getFeature(choosenDim));
                if (tempBound < model.region[choosenDim][1]) {
                    model.region[choosenDim][1] = tempBound;
                    int i = 0;
                    while (i < insLeft) {
                        if (Pop[i].getFeature(choosenDim) >= tempBound) {
                            insLeft--;
                            Instance tempins = Pop[i];
                            Pop[i] = Pop[insLeft];
                            Pop[insLeft] = tempins;
                        } else {
                            i++;
                        }
                    }
                }
            } else {
                tempBound = ro.getDouble(Pop[choosenNeg].getFeature(choosenDim), pos.getFeature(choosenDim));
                if (tempBound > model.region[choosenDim][0]) {
                    model.region[choosenDim][0] = tempBound;
                    int i = 0;
                    while (i < insLeft) {
                        if (Pop[i].getFeature(choosenDim) <= tempBound) {
                            insLeft--;
                            Instance tempins = Pop[i];
                            Pop[i] = Pop[insLeft];
                            Pop[insLeft] = tempins;
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

        int size = dimension.getSize();
        ;
        //select model dimension
        //		if(pos.getValue()>0){
        //			int seg=infeaspoint/10;
        //			if(seg==totalPoint) size=1+seg*6;
        //			else size=1+(seg+1)*6;
        //		}
        for (int k = 0; k < uncertainBits; k++) {
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
        for (int k = 0; k < dimension.getSize() - uncertainBits - 1; k++) {
            tempLab = ro.getInteger(0, labelMarkNum - 1);
            model.label[labelMark[tempLab]] = true;
            labelMark[tempLab] = labelMark[labelMarkNum - 1];
            labelMarkNum--;
        }
        //model.label[0]=false;
        return;

    }

    /**
     * if each instance in Pop is not in model, return true; if exist more than one instance in Pop is in the model,
     * return false
     *
     * @return true or false
     */
    protected boolean Distinguish() {
        int j;
        for (int i = 0; i < this.sampleSize; i++) {
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
                return false;
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
        int i, j;
        for (i = 0; i < this.positiveNum; i++) {
            if (ins.Equal(PosPop[i])) {
                return false;
            }
        }
        for (i = 0; i < this.sampleSize; i++) {
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
        Instance TempIns = new Instance(dimension);
        int j;
        for (int i = 0; i < this.sampleSize; i++) {
            for (j = 0; j < this.positiveNum; j++) {
                if (PosPop[j].getValue() > Pop[i].getValue()) {
                    break;
                }
            }
            if (j < this.positiveNum) {
                TempIns = Pop[i];
                Pop[i] = PosPop[this.positiveNum - 1];
                for (int k = this.positiveNum - 1; k > j; k--) {
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

        for (j = 0; j < this.positiveNum; j++) {
            if (PosPop[j].getValue() > temp.getValue()) {
                break;
            }
        }
        if (j < this.positiveNum) {
            TempIns = temp;
            temp = PosPop[this.positiveNum - 1];
            for (int k = this.positiveNum - 1; k > j; k--) {
                PosPop[k] = PosPop[k - 1];
            }
            PosPop[j] = TempIns;
        }

        for (j = 0; j < this.sampleSize; j++) {
            if (Pop[j].getValue() > temp.getValue()) {
                break;
            }

        }
        if (j < this.sampleSize) {
            if (this.sampleSize - 1 - j >= 0) {
                System.arraycopy(Pop, j, Pop, j + 1, this.sampleSize - 1 - j);
            }
            Pop[j] = temp;
        }
    }

    /**
     * update optimal
     */
    protected void UpdateOptimal() {
        if (Optimal.getValue() > PosPop[0].getValue()) {
            Optimal = PosPop[0];
        }
    }

    public void prepare() {
        this.BudCount = 0;
        model = new Model(dimension.getSize());
        ResetModel();
    }

    public Instance getPos() {
        ResetModel();
        ChoosenPos = ro.getInteger(0, this.positiveNum - 1);
        GlobalSample = ro.getDouble(0, 1);
        if (GlobalSample >= this.randProbability) {
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

        int bestValueCount = 0;
        int sumCount = 0;
        int iterativeNums = this.maxIteration;
        ArrayList<Double> arrayListBestValues = new ArrayList<>();

        double preBestValue = Double.MAX_VALUE;
        // for each loop
        for (int i = 1; i < maxIteration; i++) {
            double bestValue = getOptimal().getValue();
            System.out.println("i=" + i + "  best Value=" + bestValue);
            if (bestValue <= 10 || i == maxIteration - 1) {
                //task.getValue(Optimal);
                System.out.println("\ncurrent Iteration: " + i);
                break;
            }

            if (i != 1) {
                if (bestValue <= 10 && Math.abs(bestValue - preBestValue) < 0.00001) {
                    bestValueCount++;
                    if (bestValueCount > 30) {
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
            for (int j = 0; j < this.sampleSize; j++) {
                reSample = true;
                while (reSample) {
                    ResetModel();
                    ChoosenPos = ro.getInteger(0, this.positiveNum - 1);
                    GlobalSample = ro.getDouble(0, 1);
                    if (GlobalSample >= this.randProbability) {
                    } else {
                        //shrinking model
                        ShrinkModel(PosPop[ChoosenPos]);
                        //set uncertain bits
                        setRandomBits();
                    }
                    NextPop[j] = RandomInstance(PosPop[ChoosenPos]);

                    if (notExistInstance(j, NextPop[j])) {
                        double val = task.getValue(NextPop[j]);
                        NextPop[j].setValue(val);

                        reSample = false;
                    }
                }

            }
            //copy NextPop
            for (int j = 0; j < this.sampleSize; j++) {
                Pop[j] = NextPop[j];
            }
            //update PosPop set
            UpdatePosPop();
            //obtain optimal
            UpdateOptimal();
        }

        RaceZone of = (RaceZone)task;
        of.valueArc.iterativeNum = iterativeNums;
        of.valueArc.arrayListBestValues = arrayListBestValues;
        return of.valueArc;
    }

}
