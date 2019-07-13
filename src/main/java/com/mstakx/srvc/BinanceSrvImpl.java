package com.mstakx.srvc;

import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.bson.Document;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.market.OrderBookEntrySerializer;
import com.binance.api.client.domain.market.TickerPrice;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;

@Service
public class BinanceSrvImpl implements BinanceSrv {
	static MongoCollection<Document> collection  = getTable();;
	@Override
	public String do1() throws IOException {
		// TODO Auto-generated method stub
		
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("lw73x6WKXdM5YmhDOJtKOVXOXgCNnoNVkm0YbV1P3668l4J0t2GvrIpftpY3xT3P", "tC3EPxnruxZrpUJUtGL1NkZozc9Y9AhpTXgljihiVuTZB6ec9TlUftANIwPX5G2a");
		BinanceApiRestClient clientREST = factory.newRestClient();
		BinanceApiWebSocketClient clientWebSoc = factory.newWebSocketClient();
		BinanceApiAsyncRestClient clientRESTAsync = factory.newAsyncRestClient();
		clientREST.ping();
		long serverTime = clientREST.getServerTime();
		System.out.println(serverTime);
		List<TickerPrice> lst =clientREST.getAllPrices();
		ExchangeInfo exchangeInfo  =  new ExchangeInfo();
		OrderBookEntrySerializer OrderBookEntrySerializer = new OrderBookEntrySerializer();
//		JsonFactory jsonFactory = new JsonFactory();
//		JsonGenerator JsonGenerator = jsonFactory.createGenerator();
//        Writer w = new FileWriter("output.txt");  
		String s =  Utility.ObjTojson(clientREST.getOrderBook("ETHBTC", 5));
		System.out.println("<<"+s) ;
		Document doc = Document.parse(s);
		final CountDownLatch waiter = new CountDownLatch(1);

		SingleResultCallback<Void> singleResultCallback = new SingleResultCallback<Void>() {

			@Override
			public void onResult(final Void result, final Throwable t) {
				System.out.println("Success");
//				waiter.countDown();
			}
		};
		collection.insertOne(doc, singleResultCallback);
		
//		try {
//			waiter.await();
//		} catch (InterruptedException ignore) {
//		System.out.println("{\"status\":\"Unable to perform the opration\"}");
//		}
		
		
		final AtomicReference<Throwable> error = new AtomicReference<>();
	
		List<Document> resList = new ArrayList<Document>();
		SingleResultCallback<List<Document>> callbackWhenFinishedData = new SingleResultCallback<List<Document>>() {
			@Override
			public void onResult(final List<Document> result, final Throwable t) {
				if (t == null) {
					error.set(t);
				} else {
					System.out.println("success");
				}
				waiter.countDown();
			}
		};

		collection.find().maxTime(200, TimeUnit.MILLISECONDS).into(resList,
				callbackWhenFinishedData);
		try {
			waiter.await();
		} catch (InterruptedException ignore) {
			System.out.println("{\"status\":\"Unable to perform the opration\"}");
		}
		Throwable realError = error.get();
		if (realError != null)
		System.out.println("{\"status\":\"Unable to perform the opration\"}");
		System.out.println("size : " + resList.size());
		for (int i = 0; i < resList.size(); i++) {
			System.out.println(">>>>"+(resList.get(i).toJson()));
		}
//		System.out.println("??"+exchangeInfo.getSymbolInfo("ETHBTC"));
		
		for (TickerPrice TickerPrice : lst) {
			System.out.println(">>"+TickerPrice);
//			AllOrdersRequest ordrsReq = new AllOrdersRequest(TickerPrice.getSymbol());
//			List<Order> orders = clientREST.getAllOrders(ordrsReq);
////			List<Order> orders = clientREST.getOpenOrders(new OrderRequest(null));
//			for (Order order : orders) {
//				System.out.println("2>>"+order);
//			}
		}
//		
		
//		AllOrdersRequest ordrsReq = new AllOrdersRequest(null);
//		List<Order> orders = clientREST.getAllOrders(ordrsReq);
////		List<Order> orders = clientREST.getOpenOrders(new OrderRequest(null));
//		for (Order order : orders) {
//			System.out.println("2>>"+order);
//		}
//		AllOrdersRequest orderRequest;
//		orderRequest.s
		
//		clientREST.getAllOrders(orderRequest);
		return "HAAAA";
		
	}
	
	private static MongoCollection<Document> getTable() {
		MongoClient mongo = MongoClients.create();

		MongoDatabase db = mongo.getDatabase("testdb");
		MongoCollection<Document> collection = db.getCollection("test2");
		return collection;
	}

}
