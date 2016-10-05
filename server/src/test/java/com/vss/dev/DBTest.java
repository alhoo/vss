package com.vss.dev;



import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import rx.observers.TestSubscriber;

import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Row;

import rx.Observable;
import rx.observables.ConnectableObservable;

import com.github.pgasync.ConnectionPool;

/**
 * Unit test for simple App.
 */
public class DBTest 
    extends TestCase
{
	private static ConnectionPoolBuilder poolbuilder;
	private static ConnectionPool pool;
	private static int size;
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DBTest( String testName )
    {
        super( testName );
        size = 5;
        poolbuilder =  new ConnectionPoolBuilder()
        		.database("vss")
                .username("roots")
                .password("aslkj3lkgfajw")
                .poolSize(size);
        pool = poolbuilder.build();


    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DBTest.class );
    }

    /**
     * 
     */
    public void testDB()
    {
    	TestSubscriber<String> ts = TestSubscriber.create();

    	pool.queryRows("SELECT name FROM Users")
    		.take(1)
    		.map(result -> result.getString("name"))
    		.subscribe(ts);

    	ts.awaitTerminalEvent();
    	ts.assertTerminalEvent();
    	ts.assertNoErrors();
    	ts.assertCompleted();
    	ts.assertValue("lasse");
    }
}
