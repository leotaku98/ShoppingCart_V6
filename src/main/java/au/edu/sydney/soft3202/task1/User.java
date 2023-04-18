package au.edu.sydney.soft3202.task1;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String name;
    public List<Item> items;

    public User(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }

    public void addItem(Item item) {
        this.items.add(item);
    }

    public void removeItem(Item item) {
        this.items.remove(item);
    }
    public String getName(){
        return this.name;
    }
    public List<Item> getItem(){
        return this.items;
    }

}
