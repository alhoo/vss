/* 
 * Copyright (C) 2016-2017 Lasse Hyyrynen
 */
package com.vss.dev;

//#high-level-server-example

import akka.NotUsed;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.AccessControlAllowOrigin;
import akka.http.javadsl.model.headers.HttpOriginRange;
import akka.http.javadsl.common.EntityStreamingSupport;
import akka.http.javadsl.common.JsonEntityStreamingSupport;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.ConnectionPool;


/**
 * @author Lasse Hyyrynen <hyyrynen.lasse@gmail.com>
 * @version 0.1
 * @since 0.1
 * Copyright (C) 2016-2017 Lasse Hyyrynen
 */
public class VSSServer extends AllDirectives {
  
  private static ConnectionPoolBuilder poolbuilder;
  private static ConnectionPool pool;
  private static JsonEntityStreamingSupport compactJsonSupport;
  private static ActorSystem system;
  /**
   * Main function for running the VSS backend server code.
   * @param args Arguments for controlling the server initialization 
   */
  public static void main(String[] args) throws IOException {
	    // boot up server using the route as defined below

	    system = ActorSystem.create();
	    int port = 8070;
	    int size = 8;
	    // HttpApp.bindRoute expects a route being provided by HttpApp.createRoute
	    final VSSServer app = new VSSServer();

	    final Http http = Http.get(system);
	    final ActorMaterializer materializer = ActorMaterializer.create(system);

	    final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
	    final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("0.0.0.0", port), materializer);
	    
	    final ByteString none = ByteString.fromString("");
	    final ByteString between = ByteString.fromString("\r\n");
	    final Flow<ByteString, ByteString, NotUsed> compactArrayRendering = 
	    		Flow.of(ByteString.class).intersperse(none, between, none);
	    
	    //http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0.4/java/http/routing-dsl/marshalling.html
	    compactJsonSupport = EntityStreamingSupport.json()
	      .withFramingRendererFlow(compactArrayRendering);
	    
	    poolbuilder =  new ConnectionPoolBuilder()
	    		.database("vss")
	            .username("roots")
	            .password("aslkj3lkgfajw")
	            .poolSize(size);
	    pool = poolbuilder.build();
	    
	  	//Logger globalLogger = Logger.getLogger("global");
	  	/*
	  	Handler[] handlers = globalLogger.getHandlers();
	  	for(Handler handler : handlers) {
	      globalLogger.removeHandler(handler);
	  	}
	  	*/
	  	//globalLogger.setUseParentHandlers(false);
	    Runtime.getRuntime().addShutdownHook(new Thread() {
	    	public void run(){
	    		System.out.println("Graceful shutdown sequence initiated");
	    		binding
	    			.thenCompose(ServerBinding::unbind)
	    			.thenAccept(unbound -> system.terminate());
	    	}
	    });
  }

  /**
   * Create the service RESTful crud route handling.
   * <p>
   * This function outlines the higher level RESTful endpoints and
   * defines which classes handle each of them.
   * </p>
   * @return Route structure used to determine how requests are handled.
 * @throws IOException 
   */
  public Route createRoute() {
	AccessControlAllowOrigin cors = AccessControlAllowOrigin.create( HttpOriginRange.ALL );

	try {
		final ItemList<User> users = new ItemList<User>(new User());
		final ItemList<Group> groups = new ItemList<Group>(new Group());
		//final ItemList<Sensor> sensors = new ItemList<Sensor>(new Sensor());
		final SensorList sensors = new SensorList(new Sensor(), system);
		//final EventList events = new EventList(new Event(), system);
		final ItemList<Event> events = new ItemList<Event>(new Event());
		final Authenticate authenticate = new Authenticate("../private.pem"); 
//		Authorization auth = Authorization.create(HttpCredentials.create("Bearer", encode("lasse")));;
		final Function<Optional<ProvidedCredentials>, CompletionStage<Optional<Integer>>> UserAuthenticator2 = opt ->{
			if(opt.isPresent()) {
				try {
					Jws<Claims> c = authenticate.decode(opt.get().identifier());
					return CompletableFuture.completedFuture(Optional.of(Integer.parseInt(c.getBody().getSubject())));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return CompletableFuture.completedFuture(Optional.of(-1));
				//return Optional.of("nobody");
			}
			return CompletableFuture.completedFuture(Optional.of(-1));
		};
	    return route(
	    		respondWithHeader(cors, () ->
	    			extractRequest(request -> 
	    				//authenticateOAuth2Async("VSS", authenticate.UserAuthenticator, uid -> {
	    				authenticateOAuth2Async("VSS", UserAuthenticator2, uid -> {
	    					VSSRequests req = new VSSRequests();
	    					req.http = request;
	    					req.uid = uid;
	    					return route(
	    		    	authenticate.createRoute(req, pool),
	          groups.createRoute(req, pool, compactJsonSupport),
	          users.createRoute(req, pool, compactJsonSupport),
	          sensors.createRoute(req, pool, compactJsonSupport),
	          events.createRoute(req, pool, compactJsonSupport)
	    						 );
	    	}))));
	} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
			| NoSuchMethodException | SecurityException e) {
		// TODO Auto-generated catch blockd
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
  }
}
//#high-level-server-example
