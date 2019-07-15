package com.mstakx.srvc;

import java.io.IOException;
import java.util.List;

import org.bson.Document;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
@Primary
@Service
public class BinanceSrvImpl implements BinanceSrv {
	
	static MongoCollection<Document> collection  = getTable();
	static List<TickerPrice> lst;


	
		 
	  @Override
		public String do1() throws IOException, InterruptedException {
			// TODO Auto-generated method stub
		  start();
		  
		  return "Done";
		}
	 
	  private static MongoCollection<Document> getTable() {
			MongoClient mongo = MongoClients.create();

			MongoDatabase db = mongo.getDatabase("testdb");
			MongoCollection<Document> collection = db.getCollection("test13");
			return collection;
		}

	 
	 
	 

	private static void start() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(Cons.KEY, Cons.SECRT);
		  BinanceApiRestClient clientREST = factory.newRestClient();
		    lst =clientREST.getAllPrices();
			System.out.println("lst.size()"+lst.size());
			forAllSymbols(lst);
	}

	private static void forAllSymbols(List<TickerPrice> lst) {
		for (TickerPrice TickerPrice : lst) {
			new DepthCacheExample(TickerPrice.getSymbol());
		}
	}

	  

	

}
