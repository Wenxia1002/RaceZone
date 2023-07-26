package Racos.Method;

import Racos.Tools.*;

import java.util.ArrayList;

import Racos.Componet.*;
import Racos.ObjectiveFunction.*;

public class Continue extends BaseParameters {

    private final Task task;
    private final Dimension dimension;
    private Instance[] pop;
    private Instance[] nextPop;
    private Instance[] posPop;
    private Instance optimal;

    private Model model;
    private final RandomOperator ro;

    public void copyNextPop() {
        //copy NextPop
        if (this.sampleSize >= 0) {
            System.arraycopy(nextPop, 0, pop, 0, this.sampleSize);
        }
        updatePosPop();//update PosPop set
        updateOptimal();//obtain optimal
    }

    private static class Model {  //the model of generating next instance

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

        public void printLabel() {
            for (int i = 0; i < label.length; i++) {
                if (!label[i]) {
                    System.out.print(i + " ");
                }
            }
            System.out.println();
        }
    }

    public Continue(Task task) {
        this.task = task;
        dimension = task.getDim();
        ro = new RandomOperator();
    }


    public Instance getOptimal() {
        return optimal;
    }


    public Instance randomInstance() {
        Instance ins = new Instance(dimension);
        for (int i = 0; i < dimension.getSize(); i++) {

            if (dimension.getType(i)) {
                ins.setFeature(i, ro.getDouble(dimension.getRegion(i)[0], dimension.getRegion(i)[1]));
            } else {
                ins.setFeature(i, ro.getInteger((int)dimension.getRegion(i)[0], (int)dimension.getRegion(i)[1]));
            }
        }
        return ins;
    }

    public Instance randomInstance(Instance pos) {
        Instance ins = new Instance(dimension);
        while (true) {
            for (int i = 0; i < dimension.getSize(); i++) {

                if (dimension.getType(i)) {
                    if (model.label[i]) {
                        ins.setFeature(i, pos.getFeature(i));
                    } else {
                        ins.setFeature(i, ro.getDouble(Math.max(pos.region[i][0], model.region[i][0]),
                            Math.min(pos.region[i][1], model.region[i][1])));
                    }
                } else {
                    if (model.label[i]) {
                        ins.setFeature(i, pos.getFeature(i));
                    } else {
                        int bound1 = Math.max((int)pos.region[i][0], (int)model.region[i][0]), bound2 = Math.min(
                            (int)pos.region[i][1], (int)model.region[i][1]);
                        int tmp;
                        if (bound1 > bound2) {
                            tmp = ro.getInteger((int)model.region[i][0], (int)model.region[i][1]);
                        } else {
                            tmp = ro.getInteger(bound1, bound2);
                        }
                        ins.setFeature(i, tmp);
                    }
                }

            }
            break;
        }
        return ins;
    }

    protected void initialize() {

        Instance[] temp = new Instance[sampleSize + positiveNum];

        pop = new Instance[sampleSize];

        //sample Sample+PositiveNum instances and add them into temp
        for (int i = 0; i < sampleSize + positiveNum; i++) {
            temp[i] = randomInstance();
            double val = task.getValue(temp[i]);
            temp[i].setValue(val);

        }

        //sort Pop according to objective function value
        InstanceComparator comparator = new InstanceComparator();
        java.util.Arrays.sort(temp, comparator);
        java.util.Arrays.sort(temp, comparator);

        //initialize Optimal
        optimal = temp[0].CopyInstance();

        //after sorting, the beginning several instances in temp are used for initializing PosPop
        posPop = new Instance[positiveNum];
        System.arraycopy(temp, 0, posPop, 0, positiveNum);

        pop = new Instance[sampleSize];
        System.arraycopy(temp, positiveNum, pop, 0, sampleSize);

        //initialize NextPop
        nextPop = new Instance[sampleSize];

        model = new Model(dimension.getSize());

    }

    public void sortInstance(Instance[] temp) {

        //sort Pop according to objective function value
        InstanceComparator comparator = new InstanceComparator();
        java.util.Arrays.sort(temp, comparator);

        //initialize Optimal
        optimal = temp[0].CopyInstance();

        //after sorting, the beginning several instances in temp are used for initializing PosPop
        posPop = new Instance[positiveNum];
        System.arraycopy(temp, 0, posPop, 0, positiveNum);

        pop = new Instance[sampleSize];
        System.arraycopy(temp, positiveNum, pop, 0, sampleSize);

        //initialize NextPop
        nextPop = new Instance[sampleSize];

        model = new Model(dimension.getSize());

    }


    protected void resetModel() {
        for (int i = 0; i < dimension.getSize(); i++) {
            model.region[i][0] = dimension.getRegion(i)[0];
            model.region[i][1] = dimension.getRegion(i)[1];
            model.label[i] = false;
        }
    }

    protected void shrinkModel(Instance pos) {
        int choosenDim;
        int choosenNeg;
        double tempBound;

        int insLeft = sampleSize;
        int c = 0;

        while (insLeft > 0) {
            if (c > 1000) {
                System.out.println("dead loop");
            }
            ++c;
            choosenDim = ro.getInteger(0, dimension.getSize() - 1);
            choosenNeg = ro.getInteger(0, this.sampleSize - 1);
            // shrink model
            if (pos.getFeature(choosenDim) < pop[choosenNeg].getFeature(choosenDim)) {
                tempBound = ro.getDouble(pos.getFeature(choosenDim), pop[choosenNeg].getFeature(choosenDim));
                if (tempBound < model.region[choosenDim][1]) {
                    model.region[choosenDim][1] = tempBound;
                    int i = 0;
                    while (i < insLeft) {
                        if (pop[i].getFeature(choosenDim) >= tempBound) {
                            insLeft--;
                            Instance tempins = pop[i];
                            pop[i] = pop[insLeft];
                            pop[insLeft] = tempins;
                        } else {
                            i++;
                        }
                    }
                }
            } else {
                tempBound = ro.getDouble(pop[choosenNeg].getFeature(choosenDim), pos.getFeature(choosenDim));
                if (tempBound > model.region[choosenDim][0]) {
                    model.region[choosenDim][0] = tempBound;
                    int i = 0;
                    while (i < insLeft) {
                        if (pop[i].getFeature(choosenDim) <= tempBound) {
                            insLeft--;
                            Instance tempins = pop[i];
                            pop[i] = pop[insLeft];
                            pop[insLeft] = tempins;
                        } else {
                            i++;
                        }
                    }
                }
            }
        }
    }

    protected void setRandomBitsByReturnRes(Instance pos) {
        int labelMarkNum;
        int[] labelMark = new int[dimension.getSize()];
        int tempLab;
        labelMarkNum = dimension.getSize();
        for (int k = 0; k < dimension.getSize(); k++) {
            labelMark[k] = k;
            model.label[k] = true;
        }

        int size = dimension.getSize();
        for (int k = 0; k < uncertainBits; k++) {
            tempLab = ro.getInteger(0, size - 1);
            model.label[tempLab] = false;
        }

    }

    protected void setRandomBits() {
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
    }

    protected boolean distinguish() {
        int j;
        for (int i = 0; i < this.sampleSize; i++) {
            for (j = 0; j < dimension.getSize(); j++) {
                if (pop[i].getFeature(j) > model.region[j][0] && pop[i].getFeature(j) < model.region[j][1]) {

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

    protected boolean instanceIsInModel(Model model, Instance ins) {
        for (int i = 0; i < ins.getFeature().length; i++) {
            if (ins.getFeature(i) > model.region[i][1] || ins.getFeature(i) < model.region[i][0]) {
                return false;
            }
        }
        return true;
    }

    protected boolean notExistInstance(int end, Instance ins) {
        int i, j;
        for (i = 0; i < this.positiveNum; i++) {
            if (ins.Equal(posPop[i])) {
                return false;
            }
        }
        for (i = 0; i < this.sampleSize; i++) {
            if (ins.Equal(pop[i])) {
                return false;
            }
        }
        for (i = 0; i < end; i++) {
            if (ins.Equal(nextPop[i])) {
                return false;
            }
        }
        return true;

    }

    protected void updatePosPop() {
        Instance tempIns = new Instance(dimension);
        int j;
        for (int i = 0; i < this.sampleSize; i++) {
            for (j = 0; j < this.positiveNum; j++) {
                if (posPop[j].getValue() > pop[i].getValue()) {
                    break;
                }
            }
            if (j < this.positiveNum) {
                tempIns = pop[i];
                pop[i] = posPop[this.positiveNum - 1];
                for (int k = this.positiveNum - 1; k > j; k--) {
                    posPop[k] = posPop[k - 1];
                }
                posPop[j] = tempIns;
            }
        }
    }


    protected void updateOptimal() {
        if (optimal.getValue() > posPop[0].getValue()) {
            optimal = posPop[0];
        }
    }

    public void prepare() {
        model = new Model(dimension.getSize());
        resetModel();
    }

    public ValueArc run() {

        model = new Model(dimension.getSize());

        resetModel();
        initialize();

        int bestValueCount = 0;
        int iterativeNums = this.maxIteration;
        ArrayList<Double> arrayListBestValues = new ArrayList<>();

        double preBestValue = Double.MAX_VALUE;
        // for each loop
        for (int i = 1; i < maxIteration; i++) {
            double bestValue = getOptimal().getValue();
            System.out.println("i=" + i + "  best Value=" + bestValue);
            if (bestValue <= 10 || i == maxIteration - 1) {
                System.out.println("\ncurrent Iteration: " + i);
                break;
            }

            if (i != 1) {
                if (bestValue <= 90 && Math.abs(bestValue - preBestValue) < 0.00001) {
                    bestValueCount++;
                    if (bestValueCount > 30) {
                        iterativeNums = i;
                        task.getValue(optimal);
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
                boolean reSample = true;
                while (reSample) {
                    resetModel();
                    int selectedPos = ro.getInteger(0, this.positiveNum - 1);
                    double globalSample = ro.getDouble(0, 1);
                    if (globalSample >= this.randProbability) {
                    } else {
                        //shrinking model
                        shrinkModel(posPop[selectedPos]);
                        //set uncertain bits
                        setRandomBits();
                    }
                    nextPop[j] = randomInstance(posPop[selectedPos]);

                    if (notExistInstance(j, nextPop[j])) {
                        double val = task.getValue(nextPop[j]);
                        nextPop[j].setValue(val);

                        reSample = false;
                    }
                }

            }
            //copy NextPop
            if (this.sampleSize >= 0) {
                System.arraycopy(nextPop, 0, pop, 0, this.sampleSize);
            }
            //update PosPop set
            updatePosPop();
            //obtain optimal
            updateOptimal();
        }

        RaceZone of = (RaceZone)task;
        of.valueArc.iterativeNum = iterativeNums;
        of.valueArc.arrayListBestValues = arrayListBestValues;
        return of.valueArc;
    }

}
