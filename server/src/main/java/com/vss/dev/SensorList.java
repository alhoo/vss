package com.vss.dev;

import static akka.http.javadsl.unmarshalling.StringUnmarshallers.INTEGER;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.lightcouch.CouchDbClient;
import org.lightcouch.Document;
import org.lightcouch.Response;

import com.github.pgasync.ConnectionPool;
import com.github.pgasync.Row;

import akka.actor.ActorSystem;
import akka.http.javadsl.common.JsonEntityStreamingSupport;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.RouteResult;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.RxReactiveStreams;

public class SensorList extends ItemList<Sensor> {
	private CouchDbClient dbClient;
	private ActorSystem system;
	
	public SensorList(Sensor ev) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		super(ev);
		// TODO Auto-generated constructor stub
		dbClient = new CouchDbClient("vssdb", true, "http", "127.0.0.1", 5984, "roots", "aslkj3lkgfajw");
		//LOGGER.setUseParentHandlers(false);
	}
	public SensorList(Sensor ev, ActorSystem _system) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
			super(ev);
			system = _system;
			
		// TODO Auto-generated constructor stub
		dbClient = new CouchDbClient("vssdb", true, "http", "127.0.0.1", 5984, "roots", "aslkj3lkgfajw");
	}
	protected Route createEvent(VSSRequests req, ConnectionPool pool, JsonEntityStreamingSupport compactJsonSupport){
		StringBuilder sb = new StringBuilder("INSERT INTO Events")
							   .append(req.item.dbkeys())
							   .append("VALUES")
							 //POSTGRESQL statement for returning the inserted item:
							   .append(req.item.dbplaceholder()).append("RETURNING *");

  	    return completeOKWithSource(
  	    		dbquery(pool, sb.toString(), req.item.dbvalues()),
  	        	Jackson.marshaller(), compactJsonSupport);	
	}
	private Object Event(Row result) {
		// TODO Auto-generated method stub
		return null;
	}
	public Route createRoute(VSSRequests req, ConnectionPool pool, JsonEntityStreamingSupport compactJavaSupport){
	    return 
	    	pathPrefix("sensors", () -> {
	    		/*
	    		LOGGER.info("sensors"
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
				post(() -> path("events", () -> {
					LOGGER.info("Using SensorList to process the request");

					Source<ByteString, Object> bytes = req.http.entity().getDataBytes();

					final Materializer mat = ActorMaterializer.create(system);
					InputStream is = bytes.runWith(StreamConverters.asInputStream(), mat);
					//String ct = req.http.getHeader("Content-type").toString();
					String ct = "application/octet-stream";
					/*
					for( HttpHeader item : req.http.getHeaders() ){
						System.out.println(item.toString());
					}
					System.out.println("Real content-type: ".concat(req.http.entity().getContentType().toString()));
					System.out.println("Got item with content-type: ".concat(ct));
					*/
					ct = req.http.entity().getContentType().toString();
					Response resp = dbClient.saveAttachment(is, "data", ct);

					String url = "/REST/files/".concat(resp.getId().toString());
					Event newev = new Event();
					newev.setType("rawinput");
					newev.setUrl(url);
					newev.setSID(req.id);
					req.item = newev;
					return createEvent(req, pool, compactJavaSupport);
				})),
				delete(() -> deleteItem(req, pool, compactJavaSupport)),
				put(() -> entity(Jackson.unmarshaller(Sensor.class), it ->
					updateItem(req, (VSSObject) it, pool, compactJavaSupport)))
				);
	}
}
