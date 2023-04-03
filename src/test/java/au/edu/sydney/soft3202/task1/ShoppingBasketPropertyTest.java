package au.edu.sydney.soft3202.task1;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeTry;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

public class ShoppingBasketPropertyTest {
    private ShoppingBasket sb;
    @BeforeEach
    @BeforeTry
    public void setup(){
        sb = new ShoppingBasket();
    }

    @Provide
    Arbitrary<String> validNames(){
        ShoppingBasket validOne = new ShoppingBasket();
        return Arbitraries.of(validOne.n);
    }
    @Provide
    Arbitrary<Integer> greaterZero() {
        return Arbitraries.integers().between(1, Integer.MAX_VALUE);
    }

    @Property
    void getValueEmptyPropertyTest() {
        assertThat(sb.getValue()).isEqualTo(null);
    }
    @Property
    public void addItemTest(@ForAll("validNames") String name, @ForAll("greaterZero") int count){
        sb.addItem(name, count);
        assertThat(sb.getValue()).isEqualTo(sb.values.get(name) * count);
    }

    @Property
    public void removeItemTest(@ForAll("validNames") String name, @ForAll("greaterZero") int count){
        sb.addItem(name, count);
        assertThat(sb.getValue()).isEqualTo(sb.values.get(name) * count);
        sb.removeItem(name, count);
        assertThat(sb.getValue()).isEqualTo(null);
    }
}
