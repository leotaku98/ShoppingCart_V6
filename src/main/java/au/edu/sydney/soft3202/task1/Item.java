package au.edu.sydney.soft3202.task1;

public class Item {
    public String name;
    public int count;
    public double cost;

    public Item(String name, int count, double cost) {
        this.name = name;
        this.count = count;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}