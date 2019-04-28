/**
 * Created by sawyerprecious on 2019-04-21.
 */
public class ConnectionGene {

    private int inNode;
    private int outNode;

    private float weight;

    private boolean expressed;

    private int innovationNo;


    public ConnectionGene(int in, int out, float weight, boolean expressed, int innovationNum) {
        inNode = in;
        outNode = out;
        this.weight = weight;
        this.expressed = expressed;
        innovationNo = innovationNum;
    }

    public ConnectionGene(ConnectionGene cg) {
        this.inNode = cg.inNode;
        this.outNode = cg.outNode;
        this.weight = cg.weight;
        this.expressed = cg.expressed;
        this.innovationNo = cg.innovationNo;
    }


    public int getInNode() {
        return inNode;
    }

    public int getOutNode() {
        return outNode;
    }

    public float getWeight() {
        return weight;
    }

    public boolean isExpressed() {
        return expressed;
    }

    public int getInnovationNo() {
        return innovationNo;
    }

    public void disable() {
        expressed = false;
    }

    public ConnectionGene copyThis() {
        return new ConnectionGene(inNode, outNode, weight, expressed, innovationNo);
    }

    public void setWeight(float newWeight) {
        weight = newWeight;
    }




}
