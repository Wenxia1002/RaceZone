package MPC;

public class WeldTask {
    private int id;//task id
    private Point start;//start point infomation
    private Point target;//target point information
    private int bound;//the bound of number of viaLocations

    public WeldTask(int id,Point s,Point t,int b){
        this.id=id;
        start=s;
        target=t;
        bound=b;
    }

    public WeldTask(Point s,Point t){
        this.id=0;
        start=s;
        target=t;
    }

    public int getBound() {
        return bound;
    }

    public int getId() {
        return id;
    }

    public double[][] getLocRegion(){
        double[][] region=new double[3][2];
//        region[0][0]=-50;
//        region[0][1]=50;
//        region[1][0]=-50;
//        region[1][1]=50;
//        region[2][0]=-50;
//        region[2][1]=50;
        region[0][0]=Math.min(start.getX()[0],target.getX()[0])-200;
        region[0][1]=Math.max(start.getX()[0],target.getX()[0])+200;
        region[1][0]=Math.min(start.getX()[1],target.getX()[1])-200;
        region[1][1]=Math.max(start.getX()[1],target.getX()[1])+200;
        region[2][0]=Math.min(start.getX()[2],target.getX()[2])-200;
        region[2][1]=Math.max(start.getX()[2],target.getX()[2])+200;

        return region;
    }

    public Point getStart() {
        return start;
    }

    public Point getTarget() {
        return target;
    }
}
