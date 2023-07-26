package Racos.Componet;

import java.util.*;

public class InstanceComparator implements Comparator {

    @Override
    public int compare(Object arg0, Object arg1) {
        Instance ins1 = (Instance)arg0;
        Instance ins2 = (Instance)arg1;

        return Double.compare(ins1.getValue(), ins2.getValue());
    }

}
