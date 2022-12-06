package models;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {

    Product stroopwafels;

    @BeforeEach
    private void setup() {
        stroopwafels = new Product(111111111111111L, "Stroopwafels 10st", 1.23);
    }


    @Test
    public void aProductHasAStringRepresentation() {
        assertEquals("111111111111111/Stroopwafels 10st/1.23", stroopwafels.toString());
    }

    @Test
    public void canConvertATextLineToAProduct() {
        Product product1 = Product.fromLine("111111111111111, Mars bar, 0.90");
        Product product2 = Product.fromLine("222222222222222, Bounty bar, 0.85, 1.25");

        assertEquals(111111111111111L, product1.getBarcode());
        assertEquals("Mars bar", product1.getTitle());
        assertEquals(0.90, product1.getPrice());
        assertEquals("Bounty bar", product2.getTitle());
        assertEquals(0.85, product2.getPrice());
    }

    @Test
    public void canCompareProducts() {
        Product product1 = Product.fromLine("111111111111111, Mars bar, 0.90");
        Product product2 = Product.fromLine("111111111111111, Mars bar, 0.90");
        Product productOtherTitle = Product.fromLine("111111111111111, Snickers bar, 0.90");
        Product productOtherPrice = Product.fromLine("111111111111111, Snickers bar, 0.99");
        Product productOtherBarcode = Product.fromLine("111111111111112, Mars bar, 0.90");

        assertTrue(product1.equals(product2));
        assertTrue(product1.equals(productOtherTitle));
        assertTrue(product1.equals(productOtherPrice));
        assertFalse(product1.equals(productOtherBarcode));
    }
}