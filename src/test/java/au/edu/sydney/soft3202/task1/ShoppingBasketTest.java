package au.edu.sydney.soft3202.task1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ShoppingBasketTest {

    ShoppingBasket shoppingBasket;

    @BeforeEach
    public void setUp() {
        shoppingBasket = new ShoppingBasket();
    }

    @Test
    public void getValueEmptyTest() {
        assertNull(shoppingBasket.getValue());
    }

    @Test
    public void addItemTest(){
        assertNull(shoppingBasket.getValue());
        shoppingBasket.addItem("apple",3);
        assertEquals(2.5*3,shoppingBasket.getValue());
        shoppingBasket.addItem("orange",1);
        assertEquals(2.5*3+1.25,shoppingBasket.getValue());
        shoppingBasket.addItem("pear",1);
        assertEquals(2.5*3+1.25+3,shoppingBasket.getValue());
        shoppingBasket.addItem("banana",1);
        assertEquals(2.5*3+1.25+3+4.95,shoppingBasket.getValue());

    }

    @Test
    public void removeItemTest(){
        shoppingBasket.addItem("apple",3);
        shoppingBasket.addItem("orange",1);
        shoppingBasket.addItem("pear",1);
        shoppingBasket.addItem("banana",1);

        shoppingBasket.removeItem("apple",3);
        assertEquals(1.25+3+4.95,shoppingBasket.getValue());
        shoppingBasket.removeItem("orange",1);
        assertEquals(3+4.95,shoppingBasket.getValue());
        shoppingBasket.removeItem("pear",1);
        assertEquals(4.95,shoppingBasket.getValue());
        shoppingBasket.removeItem("banana",1);
        assertNull(shoppingBasket.getValue());
    }
}


