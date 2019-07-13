package com.mstakx.srvc;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;

import org.bson.Document;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.DepthEvent;
import com.binance.api.client.domain.market.OrderBook;
import com.binance.api.client.domain.market.OrderBookEntry;
import com.binance.api.client.domain.market.TickerPrice;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
@Primary
@Service
public class BinanceSrvImpl implements BinanceSrv {
	
	static MongoCollection<Document> collection  = getTable();
	


	  private static final String BIDS = "BIDS";
	  private static final String ASKS = "ASKS";
	  static List<TickerPrice> lst;

//	  private  String symbol;
	  private static  BinanceApiRestClient restClient;
	  private static  BinanceApiWebSocketClient wsClient;
	  private final WsCallback wsCallback = new WsCallback();
	  private final Map <String, Map<String, NavigableMap<BigDecimal, BigDecimal>>> depthCache = new HashMap<>();

	  private long lastUpdateId = -1;
	  private volatile Closeable webSocket;
//	  @PostConstruct
//	  public void BinanceSrvImpl1(String symbol) {
//	    this.symbol = symbol;
//
//	    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
//	    this.wsClient = factory.newWebSocketClient();
//	    this.restClient = factory.newRestClient();
//
//	    initialize(symbol);
//	  }

	  private void initialize(String symbol2) {
	    // 1. Subscribe to depth events and cache any events that are received.
	    final List<DepthEvent> pendingDeltas = startDepthEventStreaming(symbol2);

	    // 2. Get a snapshot from the rest endpoint and use it to build your initial depth cache.
	    initializeDepthCache(symbol2);

	    // 3. & 4. handled in here.
	    applyPendingDeltas(pendingDeltas, symbol2);
	  }

	  /**
	   * Begins streaming of depth events.
	   *
	   * Any events received are cached until the rest API is polled for an initial snapshot.
	   */
	  private List<DepthEvent> startDepthEventStreaming(String symbol) {
	    final List<DepthEvent> pendingDeltas = new CopyOnWriteArrayList<>();
	    wsCallback.setHandler(pendingDeltas::add);

	    this.webSocket = wsClient.onDepthEvent(symbol.toLowerCase(), wsCallback);

	    return pendingDeltas;
	  }

	  /**
	   * 2. Initializes the depth cache by getting a snapshot from the REST API.
	 * @param symbol2 
	   */
	  private void initializeDepthCache(String symbol2) {
	    OrderBook orderBook = restClient.getOrderBook(symbol2.toUpperCase(), 10);

	    this.lastUpdateId = orderBook.getLastUpdateId();

	    NavigableMap<BigDecimal, BigDecimal> asks = new TreeMap<>(Comparator.reverseOrder());
	    for (OrderBookEntry ask : orderBook.getAsks()) {
	      asks.put(new BigDecimal(ask.getPrice()), new BigDecimal(ask.getQty()));
	    }
	    Map<String, NavigableMap<BigDecimal, BigDecimal>> singleSym = new HashMap<>();
	    singleSym.put(ASKS, asks);
	    
	    NavigableMap<BigDecimal, BigDecimal> bids = new TreeMap<>(Comparator.reverseOrder());
	    for (OrderBookEntry bid : orderBook.getBids()) {
	      bids.put(new BigDecimal(bid.getPrice()), new BigDecimal(bid.getQty()));
	    }
	    singleSym.put(BIDS, bids);
	    depthCache.put(symbol2, singleSym);
	  }

	  /**
	   * Deal with any cached updates and switch to normal running.
	 * @param symbol2 
	   */
	  private void applyPendingDeltas(final List<DepthEvent> pendingDeltas, String symbol2) {
	    final Consumer<DepthEvent> updateOrderBook = newEvent -> {
	      if (newEvent.getFinalUpdateId() > lastUpdateId) {
	        System.out.println(newEvent);
	        lastUpdateId = newEvent.getFinalUpdateId();
	        updateOrderBook(getAsks(symbol2), newEvent.getAsks());
	        updateOrderBook(getBids(symbol2), newEvent.getBids());
	        printDepthCache(symbol2);
	      }
	    };

	    final Consumer<DepthEvent> drainPending = newEvent -> {
	      pendingDeltas.add(newEvent);

	      // 3. Apply any deltas received on the web socket that have an update-id indicating they come
	      // after the snapshot.
	      pendingDeltas.stream()
	          .filter(
	              e -> e.getFinalUpdateId() > lastUpdateId) // Ignore any updates before the snapshot
	          .forEach(updateOrderBook);

	      // 4. Start applying any newly received depth events to the depth cache.
	      wsCallback.setHandler(updateOrderBook);
	    };

	    wsCallback.setHandler(drainPending);
	  }

	  /**
	   * Updates an order book (bids or asks) with a delta received from the server.
	   *
	   * Whenever the qty specified is ZERO, it means the price should was removed from the order book.
	   */
	  private void updateOrderBook(NavigableMap<BigDecimal, BigDecimal> lastOrderBookEntries,
	                               List<OrderBookEntry> orderBookDeltas) {
	    for (OrderBookEntry orderBookDelta : orderBookDeltas) {
	      BigDecimal price = new BigDecimal(orderBookDelta.getPrice());
	      BigDecimal qty = new BigDecimal(orderBookDelta.getQty());
	      if (qty.compareTo(BigDecimal.ZERO) == 0) {
	        // qty=0 means remove this level
	        lastOrderBookEntries.remove(price);
	      } else {
	        lastOrderBookEntries.put(price, qty);
	      }
	    }
	  }

	  public NavigableMap<BigDecimal, BigDecimal> getAsks(String symbol2) {
	    return depthCache.get(symbol2).get(ASKS);
	  }

	  public NavigableMap<BigDecimal, BigDecimal> getBids(String symbol2) {
	    return depthCache.get(symbol2).get(BIDS);
	  }

	  /**
	   * @return the best ask in the order book
	   */
	  private Map.Entry<BigDecimal, BigDecimal> getBestAsk(String symbol2) {
	    return getAsks(symbol2).lastEntry();
	  }

	  /**
	   * @return the best bid in the order book
	   */
	  private Map.Entry<BigDecimal, BigDecimal> getBestBid(String symbol2) {
	    return getBids(symbol2).firstEntry();
	  }

	  /**
	   * @return a depth cache, containing two keys (ASKs and BIDs), and for each, an ordered list of book entries.
	   */
	  public Map<String, NavigableMap<BigDecimal, BigDecimal>> getDepthCache(String symbol2) {
	    return depthCache.get(symbol2);
	  }

	  public void close() throws IOException {
	    webSocket.close();
	  }

	  /**
	   * Prints the cached order book / depth of a symbol as well as the best ask and bid price in the book.
	 * @param symbol2 
	   */
	  private void printDepthCache(String symbol2) {
		System.out.println("for symbol2 = "+symbol2);
	    try {
	    	String s= Utility.ObjTojson(depthCache);
			System.out.println(s);
			save(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    System.out.println("ASKS:(" + getAsks(symbol2).size() + ")");
	    getAsks(symbol2).entrySet().forEach(entry -> System.out.println(toDepthCacheEntryString(entry)));
	    System.out.println("BIDS:(" + getBids(symbol2).size() + ")");
	    getBids(symbol2).entrySet().forEach(entry -> System.out.println(toDepthCacheEntryString(entry)));
	    System.out.println("BEST ASK: \n" + toDepthCacheEntryString(getBestAsk(symbol2)));
	    System.out.println("BEST BID: \n" + toDepthCacheEntryString(getBestBid(symbol2)));
	    System.out.println("End for symbol2 = "+symbol2);
	    getAll();
	    
	    
	  }

	  /**
	   * Pretty prints an order book entry in the format "price / quantity".
	   */
	  private static String toDepthCacheEntryString(Map.Entry<BigDecimal, BigDecimal> depthCacheEntry) {
	    return depthCacheEntry.getKey().toPlainString() + " / " + depthCacheEntry.getValue();
	  }

	  public static void main(String[] args) {
		  

		    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
		    wsClient = factory.newWebSocketClient();
		    restClient = factory.newRestClient();

//		    initialize(symbol);
//		  start();
	    
	  }

	private  void start() {
		BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(Cons.KEY, Cons.SECRT);
		  BinanceApiRestClient clientREST = factory.newRestClient();
		    lst =clientREST.getAllPrices();
			System.out.println("lst.size()"+lst.size());
			forAllSymbols(lst);
	}

	private  void forAllSymbols(List<TickerPrice> lst) {
		for (TickerPrice TickerPrice : lst) {
			 initialize(TickerPrice.getSymbol());
		}
	}

	  private final class WsCallback implements BinanceApiCallback<DepthEvent> {

	    private final AtomicReference<Consumer<DepthEvent>> handler = new AtomicReference<>();

	    @Override
	    public void onResponse(DepthEvent depthEvent) {
	      try {
	        handler.get().accept(depthEvent);
	      } catch (final Exception e) {
	        System.err.println("Exception caught processing depth event");
	        e.printStackTrace(System.err);
	      }
	    }

	    @Override
	    public void onFailure(Throwable cause) {
	      System.out.println("WS connection failed. Reconnecting. cause:" + cause.getMessage());

	      forAllSymbols(lst);
	    }

	    private void setHandler(final Consumer<DepthEvent> handler) {
	      this.handler.set(handler);
	    }
	  }
	
	
	
	@Override
	public String do1() throws IOException, InterruptedException {
		
		

//		    initialize(symbol);
		  BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
		    wsClient = factory.newWebSocketClient();
		    restClient = factory.newRestClient();
		start();
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
		MongoCollection<Document> collection = db.getCollection("test5");
		return collection;
	}

}
