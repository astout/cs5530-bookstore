package cs5530;

import cs5530.db.DatabaseModel;

public class OrderItem extends DatabaseModel{

	public OrderItem() {
		table = "Order_Items";
		columns.add("oid");
		columns.add("ISBN");
		columns.add("count");
		columns.add("price");
		primaryKeyColumns.add("oid");
		primaryKeyColumns.add("ISBN");
	}

	public OrderItem(String oid, String isbn, String count, String price) {
		table = "Order_Items";
		columns.add("oid");
		columns.add("ISBN");
		columns.add("count");
		columns.add("price");
		primaryKeyColumns.add("oid");
		primaryKeyColumns.add("ISBN");
		
		colValPairs.put("oid", oid);
		colValPairs.put("ISBN", isbn);
		colValPairs.put("count", count);
		colValPairs.put("Price", price);
	}
}
