package com.mstakx.srvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.TickerPrice;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;

@Service
public class TestSrvImpl2 implements BinanceSrv {
	
	static MongoCollection<Document> collection  = getTable();;
	@Override
	public String do1() throws IOException, InterruptedException {
		
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(Cons.KEY, Cons.SECRT);
		BinanceApiRestClient clientREST = factory.newRestClient();
		BinanceApiWebSocketClient clientWebSoc = factory.newWebSocketClient();
		BinanceApiAsyncRestClient clientRESTAsync = factory.newAsyncRestClient();
		clientREST.ping();
		long serverTime = clientREST.getServerTime();
		System.out.println(serverTime);
		List<TickerPrice> lst =clientREST.getAllPrices();
		
		System.out.println("lst.size()"+lst.size());
		for (TickerPrice TickerPrice : lst) {
			System.out.println(">>"+TickerPrice);

			
		String s =  Utility.ObjTojson(clientREST.getOrderBook(TickerPrice.getSymbol(), 5));
		System.out.println("<<"+s) ;
		save(s);
		}
		
		
		getAll();
		
		return "Done";
		
	}

	private void getAll() {
		final CountDownLatch waiter = new CountDownLatch(1);
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
	}

	private void save(String s) {
		Document doc = Document.parse(s);
		

		SingleResultCallback<Void> singleResultCallback = new SingleResultCallback<Void>() {

			@Override
			public void onResult(final Void result, final Throwable t) {
				System.out.println("Success");
			}
		};
		collection.insertOne(doc, singleResultCallback);
	}
	
	private static MongoCollection<Document> getTable() {
		MongoClient mongo = MongoClients.create();

		MongoDatabase db = mongo.getDatabase("testdb");
		MongoCollection<Document> collection = db.getCollection("test3");
		return collection;
	}

}
