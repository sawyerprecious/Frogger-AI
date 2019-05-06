import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by sawyerprecious on 2019-04-13.
 */
public class Neuron {

    private float val;
    private LinkedHashMap<Neuron, Float> mapInputs;
    private LinkedHashMap<Neuron, Float> mapOutputs;

    public Neuron(LinkedHashMap<Neuron, Float> inputs, int v) {

        newVals(inputs, v);
        mapInputs = inputs;
        mapOutputs = new LinkedHashMap<>();

    }

    public Neuron(LinkedHashMap<Neuron, Float> inputs) {

        newVals(inputs, 2);
        mapInputs = inputs;
        mapOutputs = new LinkedHashMap<>();


    }

    public float getVal() {
        return val;
    }

    public LinkedHashMap<Neuron, Float> getInputMap() {
        return mapInputs;
    }

    public LinkedHashMap<Neuron, Float> getOutputMap() {
        return mapOutputs;
    }

    public void setVal(float val) {
        this.val = val;
    }

    public void setInputMap(LinkedHashMap<Neuron, Float> in) {
        mapInputs = in;
    }

    public void setOutputMap(LinkedHashMap<Neuron, Float> out) {
        mapInputs = out;
    }

    public void notifyOutputs() {
        for (Neuron out : mapOutputs.keySet()) {
            out.recalcVal();
        }
    }

    public void recalcVal() {
        float currVal = 0;

        for (Neuron in : mapInputs.keySet()) {
            currVal = currVal + (in.val * mapInputs.get(in));
        }

        val = currVal;
    }

    public void update(HashMap<Neuron, Float> inputs, int v) {
        newVals(inputs, v);
    }

    private void newVals(HashMap<Neuron, Float> inputs, int v) {


        if (v == 2) {

            float weightedTotal = 0;

            for(Map.Entry<Neuron, Float> entry : inputs.entrySet()) {
                Neuron key = entry.getKey();
                Float value = entry.getValue();

                weightedTotal = weightedTotal + (key.val * value);

            }

            val = weightedTotal;

        } else {

            val = v;
        }
    }

    public void adjustVals() {

        if (mapInputs.entrySet().isEmpty()) {

            return;

        } else {

            float weightedTotal = 0;

            for (Map.Entry<Neuron, Float> entry : mapInputs.entrySet()) {

                Neuron key = entry.getKey();
                Float value = entry.getValue();

                weightedTotal = weightedTotal + (key.val * value);

            }

            val = weightedTotal;
        }

    }
}


