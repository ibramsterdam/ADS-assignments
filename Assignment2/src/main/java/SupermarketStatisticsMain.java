import models.Purchase;
import models.PurchaseTracker;

import java.util.Comparator;

public class SupermarketStatisticsMain {

    public static void main(String[] args) {
        System.out.println("Welcome to the HvA Supermarket Statistics processor\n");

        PurchaseTracker purchaseTracker = new PurchaseTracker();

        purchaseTracker.importProductsFromVault("/products.txt");

        purchaseTracker.importPurchasesFromVault("/purchases");

        purchaseTracker.showTops(5, "worst sales volume",
                Comparator.comparing(Purchase::getCount)
        );

        purchaseTracker.showTops(5, "best sales revenue",
                Comparator.comparing(Purchase::getTotalValue).reversed()
        );

        purchaseTracker.showTotals();
    }

}
