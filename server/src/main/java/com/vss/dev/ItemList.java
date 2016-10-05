package com.vss.dev;

import akka.http.javadsl.common.JsonEntityStreamingSupport;
import static akka.http.javadsl.unmarshalling.StringUnmarshallers.INTEGER;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.javadsl.Source;
import edu.emory.mathcs.backport.java.util.Arrays;
import rx.Observable;
import rx.RxReactiveStreams;

import com.github.pgasync.ConnectionPool;
import com.github.pgasync.Row;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import java.util.logging.Logger;

enum Action {GET, POST, PUT, DELETE};

/*
 * vss=> select * from users u, groups g, sensors s, events e where u.gID = g.ID and s.gID = g.ID and e.sID = s.ID;
 * vss=> select * from users u1, groups g, users u2 where u1.ID = 1 and u1.gID = g.ID and u2.gID = g.ID;
 */
/**
 *
 * @author Lasse Hyyrynen <hyyrynen.lasse@gmail.com>
 * @version 0.1
 * @since 0.1
 * @param <T> Type of object that we are controlling.
 */
public class ItemList<T> extends AllDirectives {
	private T item;
	
	private String classname;
	private String classname_plural;
	protected Logger LOGGER;
	
	/**
	 * Create a handler for a given type of object.
	 * @param t Type of object we are controlling.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public ItemList(T t) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		item = t;
		classname = item.getClass().getSimpleName();
		classname_plural = classname.concat("s");
		LOGGER = Logger.getLogger(t.getClass().getName());
	}
	/*
	public ItemList(T t, ActorSystem system) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
		item = t;
		classname = item.getClass().getSimpleName();
		classname_plural = classname.concat("s");
		LOGGER = Logger.getLogger(t.getClass().getName());
	}
	*/
	/**
	 * Run a database query
	 */
	protected Source<T, ?> dbquery(ConnectionPool pool, String query, Object... params) {
		Observable<T> res = 
  	       		(Observable<T>) pool.queryRows(query, params)
					.map(result -> {
						try {
							return (T) item.getClass().getConstructor(new Class<?>[] {Row.class}).newInstance(result);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | NoSuchMethodException | SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					});
		return Source.fromPublisher(RxReactiveStreams.toPublisher(res));
	}
	/**
	 * List objects of the given type.
	 * @param pool database connection pool.
	 * @param compactJsonSupport json encoder.
	 * @return A route for handling the given type.
	 */
	protected Route list(VSSRequests req, ConnectionPool pool, JsonEntityStreamingSupport compactJsonSupport){
		String sb = ((VSSObject) item).getAllQuery(); 
		
  	    return completeOKWithSource(
  	    		dbquery(pool, sb, req.uid),
  	        	Jackson.marshaller(), compactJsonSupport);
	}
	/**
	 * Create a new item of the given type.
	 * @param req request parameters.
	 * @param pool database connection pool.
	 * @param compactJsonSupport json encoder.
	 * @return A route for creating the given type.
	 */
	protected Route createItem(VSSRequests req, ConnectionPool pool, JsonEntityStreamingSupport compactJsonSupport){
		StringBuilder sb = new StringBuilder("INSERT INTO ".concat(classname_plural))
							   .append(req.item.dbkeys())
							   .append("VALUES")
							 //POSTGRESQL statement for returning the inserted item:
							   .append(req.item.dbplaceholder()).append("RETURNING *");

  	    return completeOKWithSource(
  	    		dbquery(pool, sb.toString(), req.item.dbvalues()),
  	        	Jackson.marshaller(), compactJsonSupport);	
	}
	/**
	 * Get a item of the given type.
	 * @param id object identifier.
	 * @param pool database connection pool.
	 * @param compactJsonSupport json encoder.
	 * @return A route for getting an existing object of the given type.
	 */
	protected Route getItem(VSSRequests req, ConnectionPool pool, JsonEntityStreamingSupport compactJsonSupport){
		String sb = ((VSSObject) item).getQuery();

  	    return completeOKWithSource(
  	    		dbquery(pool, sb.toString(), req.uid, req.id),
  	        	Jackson.marshaller(), compactJsonSupport);	
	}
	/**
	 * Delete an existing item of the given type.
	 * @param id object identifier.
	 * @param pool database connection pool.
	 * @param compactJsonSupport json encoder.
	 * @return A route for removing a item of the given type.
	 */
	protected Route deleteItem(VSSRequests req, ConnectionPool pool, JsonEntityStreamingSupport compactJsonSupport){
		StringBuilder sb = new StringBuilder("UPDATE ".concat(classname_plural));

  	    return completeOKWithSource(
  	    		dbquery(pool, sb.append(" SET state = 'disabled' WHERE ID = $1").toString(), req.id),
  	        	Jackson.marshaller(), compactJsonSupport);	
	}
	private Route enableItem(VSSRequests req, ConnectionPool pool, JsonEntityStreamingSupport compactJsonSupport){
		StringBuilder sb = new StringBuilder("UPDATE ".concat(classname_plural));

  	    return completeOKWithSource(
  	    		dbquery(pool, sb.append(" SET state = 'active' WHERE ID = $1").toString(), req.id),
  	        	Jackson.marshaller(), compactJsonSupport);	
	
	}
	protected Route updateItem(VSSRequests req, VSSObject item2, ConnectionPool pool, JsonEntityStreamingSupport compactJsonSupport){
		StringBuilder sb = new StringBuilder("UPDATE ".concat(classname_plural))
					.append(" SET")
				   .append(item2.dbkeys())
				   .append("=")
				 //POSTGRESQL statement for returning the inserted item:
				   .append(item2.dbplaceholder(1));
		Vector<String> values = new Vector<String>(Arrays.asList(item2.dbvalues()));
		values.add(0, Integer.toString(req.id));

  	    return completeOKWithSource(
  	    		dbquery(pool, sb.append("WHERE ID = $1 RETURNING *").toString(), values.toArray()),
  	        	Jackson.marshaller(), compactJsonSupport);	
	}
	/**
	 * Create a route for the given type.
	 * @param request user request object.
	 * @param pool database connection pool.
	 * @param compactJavaSupport json encoder.
	 * @return route for handling the request.
	 */
	public Route createRoute(VSSRequests req, ConnectionPool pool, JsonEntityStreamingSupport compactJavaSupport){
	    return 
	    	pathPrefix(classname_plural.toLowerCase(), () -> {
	    		/*
	    		LOGGER.info(item.getClass().getName()
	    				.concat(" ")
	    				.concat(req.http.method().name())
	    				.concat(" (from uid: ")
	    				.concat(req.uid.toString())
	    				.concat(" ) : ")
	    				.concat(req.http.getUri().toString())
	    				);
	    		*/
	    		return route(
	          pathSingleSlash(() ->
	            applyToList(req, pool, compactJavaSupport)
	          ),
	          pathEnd(() ->
	            applyToList(req, pool, compactJavaSupport)
	          ),
	          pathPrefix(INTEGER, iid ->
	            applyToItem(req, iid, pool, compactJavaSupport)
	          )
	        ); });
	}
	protected Route applyToItem(VSSRequests req, Integer id, ConnectionPool pool,
			JsonEntityStreamingSupport compactJavaSupport) {
		req.id = id;
		//LOGGER.info("ItemList::applyToItem()");
		return route(
				get(() -> getItem(req, pool, compactJavaSupport)),
				//post(() -> complete("OK")),
				delete(() -> deleteItem(req, pool, compactJavaSupport)),
				put(() -> entity(Jackson.unmarshaller(item.getClass()), it ->
					updateItem(req, (VSSObject) it, pool, compactJavaSupport)))
				);
	}
	protected Route applyToList(VSSRequests req, ConnectionPool pool,
			JsonEntityStreamingSupport compactJavaSupport) {
		return route(
				get(() -> list(req, pool, compactJavaSupport)),
				post(() -> entity(Jackson.unmarshaller(item.getClass()), it -> {
					req.item = (VSSObject) it;
					return createItem(req, pool, compactJavaSupport);
				}))
				//,
				//delete(() -> deleteItem(req, pool, compactJavaSupport)),
				//put(() -> entity(Jackson.unmarshaller(item.getClass()), it ->
				//	updateItem(req, (VSSObject) it, pool, compactJavaSupport)))
				);
	}
}

