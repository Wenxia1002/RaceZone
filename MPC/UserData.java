package MPC;

public class UserData {
    String id;
    double abr;
    double dpv;
    double ctr;

    public UserData(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAbr() {
        return abr;
    }

    public void setAbr(double abr) {
        this.abr = abr;
    }

    public double getDpv() {
        return dpv;
    }

    public void setDpv(double dpv) {
        this.dpv = dpv;
    }

    public double getCtr() {
        return ctr;
    }

    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

    public UserData(String id, String abr,String dpv ,String ctr){
        this.id=id;
        this.abr=Double.parseDouble(abr);
        this.dpv=Double.parseDouble(dpv);
        this.ctr=Double.parseDouble(ctr);
    }




}
