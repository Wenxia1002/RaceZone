package Racos.ObjectiveFunction;

import MPC.Point;
import MPC.WeldTask;
import Racos.Componet.Dimension;
import Racos.Componet.Instance;
import Racos.Tools.ValueArc;

import java.io.*;
import java.util.ArrayList;

public class Weld implements Task {
    public ValueArc valueArc;
    private Dimension dim;//parameters dimensions
    private int viaBound;//the bound of the number of viaPoints
    private Point start;
    private Point target;
    private String viaLocFile;//file saves the information of all viapoints
    private String resultFile;//the result from the program of crush detection
    private boolean sat;//if the viapoints are vaild
    private double max_num;//constant using to map invaild instance into positive
    private String name;
    public int param4each=3;

    public Point getStart(){
        return start;
    }

    public Point getTarget(){
        return target;
    }
    public Weld(String root, WeldTask weldTask){
        viaBound= weldTask.getBound();
        start=weldTask.getStart();
        target=weldTask.getTarget();
        max_num=5000;
        viaLocFile=root+"viaLoc.csv";
        resultFile=root+"result.csv";

        dim = new Dimension();
        dim.setSize(viaBound*6);
        dim.setSize(1+viaBound*6);

        dim.setDimension(0,1,viaBound,false);//the first dimension resticts the number of vialocations
        int index=1;
        double[][] region= weldTask.getLocRegion();
        for(int i=0;i<viaBound;i++){
            //Position range
            dim.setDimension(index,region[0][0],region[0][1],true);
            dim.setDimension(index+1,region[1][0],region[1][1],true);
            dim.setDimension(index+2,region[2][0],region[2][1],true);
            //Euler Angle range
            dim.setDimension(index+3,-Math.PI/10,Math.PI/10,true);
            dim.setDimension(index+4,-Math.PI/2,Math.PI/2,true);
            dim.setDimension(index+5,-Math.PI,Math.PI,true);
            index+=6;
        }

    }

    public String getId(){
        return name;
    }

    public Weld(String[] start, String[] target,int bound){
        name=start[0];
        this.start=new Point(start);
        this.target=new Point(target);
        viaBound=bound;
        WeldTask weld=new WeldTask(this.start,this.target);
        dim = new Dimension();
        dim.setSize(1+viaBound*param4each);

        dim.setDimension(0,2,2,false);//the first dimension resticts the number of vialocations
        int index=1;
        double[][] region= weld.getLocRegion();
        for(int i=0;i<viaBound;i++){
            //Position range
            dim.setDimension(index+0,region[0][0],region[0][1],true);
            dim.setDimension(index+1,region[1][0],region[1][1],true);
            dim.setDimension(index+2,region[2][0],region[2][1],true);
            //Euler Angle range
//            dim.setDimension(index+3,-180.0,180.0,true);
//            dim.setDimension(index+4,-180.0,180.0,true);
//            dim.setDimension(index+5,-180.0,180.0,true);
            index+=param4each;
        }


    }
    //for calculating objective function
    public double getValue(Instance ins){
        sat=true;
        int via_num= (int) ins.getFeature(0);//the number of vialocations
        int index=1;
        ArrayList<String> data=new ArrayList<>();
        addPoint(data,start);
        for(int i=0;i<via_num;i++){
            String tmp=start.locName+Integer.toString(i+1)+","
                    + Double.toString(ins.getFeature(index))+","
                    + Double.toString(ins.getFeature(index+1))+","
                    + Double.toString(ins.getFeature(index+2))+","
                    + Double.toString(ins.getFeature(index+3))+","
                    + Double.toString(ins.getFeature(index+4))+","
                    + Double.toString(ins.getFeature(index+5));
            index+=6;
            data.add(tmp);
        }
        addPoint(data,target);
        write(data);


        return readResult();
    }

    private void addPoint(ArrayList<String> data, Point viaLoc) {
        String tmp=viaLoc.locName+","
                + Double.toString(viaLoc.getX()[0])+","
                + Double.toString(viaLoc.getX()[1])+","
                + Double.toString(viaLoc.getX()[2])+","
                + Double.toString(viaLoc.getR()[0])+","
                + Double.toString(viaLoc.getR()[1])+","
                + Double.toString(viaLoc.getR()[2]);

        data.add(tmp);
    }


    public void write(ArrayList<String> dataList){

        FileOutputStream out=null;
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;
        try {
            out = new FileOutputStream(viaLocFile);
            osw = new OutputStreamWriter(out);
            bw =new BufferedWriter(osw);
            if(dataList!=null && !dataList.isEmpty()){
                for(String data : dataList){
                    bw.append(data).append("\r");
                }
            }
        } catch (Exception e) {
        }finally{
            if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public double readResult(){

        File resultFile = new File(this.resultFile);

        while(!resultFile.exists())
        {
            try {
                System.out.println("Waiting....");
                Thread.sleep( 100 );
            } catch (Exception e){
                System.exit( 0 );
            }
        }

        BufferedReader reader = null;
        double tmp_result = 0;

        try {
            reader = new BufferedReader(new FileReader(resultFile));
            String DELIMITER = ",";
            String line;
            double value=0;
            int index=0;
            while ((line = reader.readLine()) != null) {
                tmp_result+=value;
                if(index!=0&&value==0){
                    sat=false;
                }
                // split the line by ','
                String[] columns = line.split(DELIMITER);
                value=Double.parseDouble(columns[1]);//distance
                if(value==0) {
                    //crash
                    value+=max_num;

                }else{
                    value=(-1)*Double.parseDouble(columns[8]);
                }
                index++;
            }
        }catch (FileNotFoundException e) {
            System.out.println("File not found" + '\n' + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Exception" + '\n' + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("IO Exception" + '\n' + e.getMessage());
                }
            }
        }


        try{
            if(resultFile.delete()){
                System.out.println(resultFile.getName() + " has been delete.");
            }else{
                System.out.println("Delete fail!");
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return tmp_result;
    }
    //for get dimension message for each task
    public Dimension getDim(){
        return dim;
    }


}
