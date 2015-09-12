import com.enjin.core.EnjinServices;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.FilteredItem;
import com.enjin.rpc.mappings.mappings.shop.Purchase;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.List;

@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class ShopServiceTest {
    private static final String API_URL = "https://api.enjin.com/api/v1/";
    private static final String KEY = "cfc9718c515f63e26804af7f56b1c966de13501ecdad1ad41e";
    private static final String PLAYER = "Favorlock";

    @Test
    public void test1Get() {
        ShopService service = EnjinServices.getService(ShopService.class);
        RPCData<List<Shop>> data = service.get(KEY, PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<Shop> shops = data.getResult();

        Assert.assertNotNull("shops is null", shops);

        System.out.println("# of shops: " + shops.size());
        for (Shop shop : shops) {
            System.out.println(shop.toString());
        }
    }

    @Test
    public void test2GetPurchases() {
        ShopService service = EnjinServices.getService(ShopService.class);
        RPCData<List<Purchase>> data = service.getPurchases(KEY, PLAYER, true);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<Purchase> purchases = data.getResult();

        Assert.assertNotNull("purchases is null", purchases);

        System.out.println("# of purchases: " + purchases.size());
        for (Purchase purchase : purchases) {
            System.out.println(purchase.toString());
        }
    }

    @Test
    public void test3GetItems() {
        ShopService service = EnjinServices.getService(ShopService.class);
        RPCData<List<FilteredItem>> data = service.getItems(KEY, PLAYER);

        Assert.assertNotNull("data is null", data);
        Assert.assertNotNull("data result is null", data.getResult());

        List<FilteredItem> items = data.getResult();

        Assert.assertNotNull("items is null", items);

        System.out.println("# of items: " + items.size());
        for (FilteredItem item : items) {
            System.out.println(item.toString());
        }
    }

    @BeforeClass
    public static void prepare() {
        EnjinRPC.setHttps(true);
        EnjinRPC.setApiUrl(API_URL);
    }
}