package models;

import java.util.List;

public class Purchase{
    private final Product product;
    private int count;

    public Purchase(Product product, int count) {
        this.product = product;
        this.count = count;
    }

    /**
     * parses purchase summary information from a textLine with format: barcode, amount
     * @param textLine line containing the barcode and amount of the product
     * @param products  a list of products ordered and searchable by barcode
     *                  (i.e. the comparator of the ordered list shall consider only the barcode when comparing products)
     * @return  a new Purchase instance with the provided information
     *          or null if the textLine is corrupt or incomplete
     */
    public static Purchase fromLine(String textLine, List<Product> products) {
        String[] purchaseInfo = textLine.split(", ");
        long barcode = Long.parseLong(purchaseInfo[0]);
        Product product = products.stream().filter(
                product1 -> product1.getBarcode() == barcode)
                .findFirst().orElse(new Product(barcode));

        return new Purchase(product, Integer.parseInt(purchaseInfo[1]));
    }

    /**
     * add a delta amount to the count of the purchase summary instance
     * @param delta the difference in count
     */
    public void addCount(int delta) {
        this.count += delta;
    }

    public long getBarcode() {
        return this.product.getBarcode();
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public Product getProduct() {
        return product;
    }
    public double getTotalValue() {
        return product.getPrice() * count;
    }

    @Override
    public String toString() {
        return  String.format(java.util.Locale.US,"%s/%s/%d/%.2f",
                product.getBarcode(), product.getTitle(), count, this.getTotalValue());
    }
}
