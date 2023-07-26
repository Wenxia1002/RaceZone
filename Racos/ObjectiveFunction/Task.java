package Racos.ObjectiveFunction;

import Racos.Componet.*;

public interface Task {

	public abstract double getValue(Instance ins);

	public abstract Dimension getDim();

}
