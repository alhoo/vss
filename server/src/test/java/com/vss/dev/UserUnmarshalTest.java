package com.vss.dev;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UserUnmarshalTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public UserUnmarshalTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( UserUnmarshalTest.class );
    }

    /**
     * Test basic user unmarshalling from json
     */
    public void testUser()
    {
        String jsonUser = "{\"name\": \"lasse\", \"pw\": \"salasana\"}";
        ObjectMapper mapper = new ObjectMapper();
        
		try {
			User u = mapper.readValue(jsonUser, User.class);
	        System.out.println(u.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
