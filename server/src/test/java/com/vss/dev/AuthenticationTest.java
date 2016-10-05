package com.vss.dev;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import scala.collection.Iterator;

public class AuthenticationTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AuthenticationTest( String testName )
    {
        super( testName );
    }
    
    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AuthenticationTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	try {
			Authenticate a = new Authenticate("../private.pem");
			User u = new User();
			u.setName("lasse");
			u.setId(1);
			u.setEmail("test@vss.fi");
			String jwt = a.encodetoken(u);
			//System.out.println(jwt);
			Jws<Claims> claims = a.decode(jwt);
			/*
			for(java.util.Iterator<String> it = claims.getBody().keySet().iterator(); it.hasNext(); ){
				String item = it.next();
				System.out.println(item);
			}
			*/
			//System.out.println("Sub: ".concat(claims.getBody().getSubject()));
			/*
			String token = "eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJsYXNzZSJ9.FuTP6r-QgllMS21fSnPaoWNA0t5Ou5XFVMKjEDaTUqzHY5UDB234Ft_qtp5Z7IACZ3wDgHKbyuT7TPFJ1o_aS61gFkoftumhgbQ64Gm4M4lojFH30_YpRbpDqZ4TWIAz9vEK7YLgB9f1NmRrjh3uOG0460n-gybcECCbAEYDnXRMvs8XtSUwAek6fly7-IIIB_is0IFMjHIH7gzWozl6yTyv1Y-BB1Zn8hXRbaMhMlxIxWcWxCXMDYPyv-BR9UGdBtWfvFWt3GCMSSnp4AaUqBzCRLqMOCAllGvWsyU8xAZYe8okJ7niJaMpnPtTPllhnGrXfBcEQFxX5TRRKLX8mQ";
			claims = a.decode(token);
			for(java.util.Iterator<String> it = claims.getBody().keySet().iterator(); it.hasNext(); ){
				String item = it.next();
				System.out.println(item);
			}
			System.out.println("Sub: ".concat(claims.getBody().getSubject()));
			*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        assertTrue( true );
    }
}
