/**
 * Created by sawyerprecious on 2019-04-21.
 */
public class NodeGene {


    enum GENETYPE {
        HIDDEN,
        INPUT,
        OUTPUT,
        ;
    }


    private GENETYPE type;
    private int id;

    public NodeGene(GENETYPE type, int id) {
        this.type = type;
        this.id = id;
    }

    public NodeGene(NodeGene ng) {
        this.type = ng.type;
        this.id = ng.id;
    }

    public GENETYPE getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public NodeGene copyThis() {
        return new NodeGene(type, id);
    }


}
