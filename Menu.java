import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Menu {
    private List<String> menuItems = new ArrayList<>();
    private List<Integer> prices = new ArrayList<>();

    public List<String> getMenuItems() {
        return menuItems;
    }

    public List<Integer> getPrices() {
        return prices;
    }

    public void display(Connection c, Statement stmt) throws Exception {
        menuItems.clear();
        prices.clear();
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/restaurant",
                            "postgres", "postgres");
            c.setAutoCommit(false);

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select * from menu;");
            while (rs.next()) {
                String name = rs.getString("dish");
                int price = rs.getInt("price");
                menuItems.add(name);
                prices.add(price);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void editMenu(Dish dish, Connection c, Statement stmt) throws Exception {
        Class.forName("org.postgresql.Driver");
        c = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/restaurant",
                        "postgres", "postgres");
        c.setAutoCommit(false);

        stmt = c.createStatement();
        String sql = "insert into menu values (dish, price) '" + dish.name + "', " + Integer.parseInt(dish.price) + ");";
        stmt.executeUpdate(sql);
        c.commit();
    }
}
