package MPC;

public class Point {
    public String locName;
    private final double x;
    private final double y;
    private final double z;
    private final double w;
    private final double p;
    private final double r;

    public Point(String[] info){
        locName=info[0];
        x=Double.parseDouble(info[1]);
        y=Double.parseDouble(info[2]);
        z=Double.parseDouble(info[3]);

        w=Double.parseDouble(info[4]);
        p=Double.parseDouble(info[5]);
        r=Double.parseDouble(info[6]);
    }
    public Point(String name, String x1,String x2,String x3,String r1,String r2,String r3){
        locName=name;
        x=Double.parseDouble(x1);
        y=Double.parseDouble(x2);
        z=Double.parseDouble(x3);

        w=Double.parseDouble(r1);
        p=Double.parseDouble(r2);
        r=Double.parseDouble(r3);
    }

    public Point(int x1,int x2,int x3,int r1,int r2,int r3){
        x=x1;
        y=x2;
        z=x3;

        w=r1;
        p=r2;
        r=r3;
    }

    public Point(double[] X, double[] R){
        x=X[0];
        y=X[1];
        z=X[2];

        w=R[0];
        p=R[1];
        r=R[2];
    }

    public double[] getX(){
        double[] X=new double[3];
        X[0]=x;
        X[1]=y;
        X[2]=z;

        return X;
    }

    public double[] getR(){
        double[] R=new double[3];
        R[0]=w;
        R[1]=p;
        R[2]=r;

        return R;
    }
}
