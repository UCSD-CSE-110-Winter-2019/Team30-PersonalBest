package team30.personalbest;

import java.io.Serializable;
import java.util.ArrayList;

import team30.personalbest.framework.snapshot.FitnessSnapshot;
import team30.personalbest.framework.snapshot.GoalSnapshot;
import team30.personalbest.framework.snapshot.IFitnessSnapshot;
import team30.personalbest.framework.snapshot.IGoalSnapshot;

public class SnapshotObject implements Serializable {

    public ArrayList<FitnessSnapshot> fitnessSnapshots;
    public ArrayList<GoalSnapshot> goalSnapshots;

    public SnapshotObject() {
        fitnessSnapshots = new ArrayList<>();
        goalSnapshots = new ArrayList<>();
    }

    public SnapshotObject(Iterable<IFitnessSnapshot> fs, Iterable<IGoalSnapshot> gs) {

        this();

        for (IFitnessSnapshot s : fs) {
            fitnessSnapshots.add((FitnessSnapshot) s);
        }

        for (IGoalSnapshot g : gs) {
            goalSnapshots.add((GoalSnapshot) g);
        }
    }

    public void setFitnessSnapshots(ArrayList<FitnessSnapshot> fitnessSnapshots) {
        for (IFitnessSnapshot s : fitnessSnapshots) {
            this.fitnessSnapshots.add((FitnessSnapshot) s);
        }
    }

    public void setGoalSnapshots(ArrayList<GoalSnapshot> goalSnapshots) {
        for (IGoalSnapshot g : goalSnapshots) {
            this.goalSnapshots.add((GoalSnapshot) g);
        }
    }


}
