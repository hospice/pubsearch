package ps.struct;

public class SearchResultWeight {

    private int id;

    private double weight;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public SearchResultWeight(int id, double weight) {
        this.id = id;
        this.weight = weight;
    }

}
