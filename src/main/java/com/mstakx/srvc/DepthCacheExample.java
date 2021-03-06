package com.mstakx.srvc;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

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
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;

/**
 * Illustrates how to use the depth event stream to create a local cache of bids/asks for a symbol.
 *
 * Snapshots of the order book can be retrieved from the REST API.
 * Delta changes to the book can be received by subscribing for updates via the web socket API.
 *
 * To ensure no updates are missed, it is important to subscribe for updates on the web socket API
 * _before_ getting the snapshot from the REST API. Done the other way around it is possible to
 * miss one or more updates on the web socket, leaving the local cache in an inconsistent state.
 *
 * Steps:
 * 1. Subscribe to depth events and cache any events that are received.
 * 2. Get a snapshot from the rest endpoint and use it to build your initial depth cache.
 * 3. Apply any cache events that have a final updateId later than the snapshot's update id.
 * 4. Start applying any newly received depth events to the depth cache.
 *
 * The example repeats these steps, on a new web socket, should the web socket connection be lost.
 */
public class DepthCacheExample {
	static MongoCollection<Document> collection  = getTable();
  private static final String BIDS = "BIDS";
  private static final String ASKS = "ASKS";
  static List<TickerPrice> lst;

  private final String symbol;
  private final BinanceApiRestClient restClient;
  private final BinanceApiWebSocketClient wsClient;
  private final WsCallback wsCallback = new WsCallback();
  private final Map <String, Map<String, NavigableMap<BigDecimal, BigDecimal>>> depthCache = new HashMap<>();

  private long lastUpdateId = -1;
  private volatile Closeable webSocket;

  public DepthCacheExample(String symbol) {
	  
    this.symbol = symbol;

    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
    this.wsClient = factory.newWebSocketClient();
    this.restClient = factory.newRestClient();

    initialize(symbol);
  }

  private void initialize(String symbol2) {
    // 1. Subscribe to depth events and cache any events that are received.
    final List<DepthEvent> pendingDeltas = startDepthEventStreaming();

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
  private List<DepthEvent> startDepthEventStreaming() {
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
    OrderBook orderBook = restClient.getOrderBook(symbol.toUpperCase(), 10);

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
	    	
	    	
	    	Map <String, Map<String, NavigableMap<String, BigDecimal>>> resToDBsymb = new HashMap<>();
	    	Map<String, NavigableMap<String, BigDecimal>> askbid =new HashMap<>();
	    	NavigableMap<String, BigDecimal> priceQty = new TreeMap<>(Comparator.reverseOrder());
	    	List<String> symb = new ArrayList<>();
	    	 for (String key : depthCache.keySet()) {
	    		
	    		  for (String key2 : depthCache.get(key).keySet()) {
	    			  for (BigDecimal key3 : depthCache.get(key).get(key2).keySet()) {
	    				  priceQty.put(key3.toString().replaceAll("\\.", "D0T"), depthCache.get(key).get(key2).get(key3));
		    				    			  }
	    			  askbid.put(key2, priceQty);
		    		}
	    		  resToDBsymb.put(key, askbid);
	    		  symb.add(key);
	    		}
	    	 
	    	 String s= Utility.ObjTojson(resToDBsymb);
	    	JSONObject j;
			try {
				j = new JSONObject(s);
				Iterator<String> keys = j.keys();
				String i = keys.next() ;
		    	j.put("symbID", i);
		    	s= j.toString();
		    	System.out.println(s);
				save(s, i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	
			
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
  private void save(String s, String i) {
		
//		try {
//			Bson b = Filters.eq("symbID", i);
//			collection.updateOne(b, new Document("$set", Document.parse(s)),
//				    new SingleResultCallback<UpdateResult>() {
//				        @Override
//				        public void onResult(final UpdateResult result, final Throwable t) {
//				            System.out.println(result.getModifiedCount());
//				        }
//				    });
//		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			Document doc = Document.parse(s);
			SingleResultCallback<Void> singleResultCallback = new SingleResultCallback<Void>() {

				@Override
				public void onResult(final Void result, final Throwable t) {
					System.out.println("Success");
				}
			};
			collection.insertOne(doc, singleResultCallback);
//		}


		
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
			System.out.println("Read From DB>>>>"+(resList.get(i).toJson()));
		}
	}
  /**
   * Pretty prints an order book entry in the format "price / quantity".
   */
  private static String toDepthCacheEntryString(Map.Entry<BigDecimal, BigDecimal> depthCacheEntry) {
    return depthCacheEntry.getKey().toPlainString() + " / " + depthCacheEntry.getValue();
  }

  public static void main(String[] args) {
	  start();
    
  }
  private static MongoCollection<Document> getTable() {
		MongoClient mongo = MongoClients.create();

		MongoDatabase db = mongo.getDatabase("testdb");
		MongoCollection<Document> collection = db.getCollection("test17");
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
}
