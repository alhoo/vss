package com.vss.dev;

import com.github.pgasync.ConnectionPool;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.headers.HttpChallenge;
import akka.http.javadsl.model.headers.WWWAuthenticate;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.SecurityDirectives.ProvidedCredentials;
import akka.http.scaladsl.common.EntityStreamingSupport;
import akka.http.scaladsl.marshalling.PredefinedToEntityMarshallers;
import akka.japi.Function;
import akka.japi.Option;
import akka.stream.javadsl.Source;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.NoSuchElementException;
import rx.Observable;
import rx.RxReactiveStreams;
import rx.Single;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.IOException;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;

public class Authenticate extends AllDirectives {
	private KeyPair kp;
	private PasswordAuth pa;
	private Logger LOGGER;
	public Authenticate(String pemfile) throws IOException, NoSuchAlgorithmException{
		LOGGER = Logger.getLogger(this.getClass().getName());
		pa = new PasswordAuth();
		InputStream res = new FileInputStream(pemfile);
		String password = "vssprivatekey";
		Security.addProvider(new BouncyCastleProvider());
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
		JcePEMDecryptorProviderBuilder decryptorBuilder = new JcePEMDecryptorProviderBuilder();
		PEMDecryptorProvider decProv = decryptorBuilder.setProvider("BC").build(password.toCharArray());
		PEMParser pp = new PEMParser(new InputStreamReader(res));
		PEMEncryptedKeyPair ekp = ((PEMEncryptedKeyPair) pp.readObject());
		pp.close();
		kp = converter.getKeyPair(ekp.decryptKeyPair(decProv));
	}
	public String encodetoken(User user) throws IOException{
		long DAY_IN_MS = 1000*60*60*24;
		String compactJws = Jwts.builder()
				.claim("name", user.getName())
				.claim("email", user.getEmail())
				.claim("createtime", user.getCreateTime())
				.setId(UUID.randomUUID().toString())
				.setSubject(user.getId().toString())
				.setIssuer("VSS")
				.setAudience("VSS")
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 7*DAY_IN_MS))
				//.compressWith(CompressionCodecs.DEFLATE)
				.signWith(SignatureAlgorithm.RS512,
						  (Key) kp.getPrivate())
				.compact();
		return compactJws;
	}
	public Jws<Claims> decode(String token) throws IOException{
		try{
			LOGGER.finest("Decoding: ".concat(token));
			Jws<Claims> c = Jwts.parser()
				.setSigningKey(kp.getPublic())
//				.decompressWith(CompressionCodecs.DEFLATE)
				.parseClaimsJws(token);
			LOGGER.finest("Decoding: SUCCESS. uid : ".concat(c.getBody().getSubject()));
			return c;
		} catch (SignatureException e){}
		return null;
	}

	public Function<Optional<ProvidedCredentials>, Optional<String>> UserAuthenticator1 = opt ->{
		if(opt.isPresent()){
			System.out.println("Provided credentials: ".concat(opt.get().toString()));
			Jws<Claims> c = decode(opt.get().toString());
			//return CompletableFuture.completedFuture(Optional.of(c.getBody().getSubject()));
			return Optional.of(c.getBody().getSubject());
		}
		return null;
	};
	public Function<Optional<ProvidedCredentials>, CompletionStage<Optional<Integer>>> UserAuthenticator = opt ->{
		if(opt.isPresent()) {
			try {
				Jws<Claims> c = decode(opt.get().identifier());
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
	public Route createRoute(VSSRequests req, ConnectionPool pool) {
		Option<String> realm = Option.option("VSS");
		WWWAuthenticate auth = WWWAuthenticate.create(HttpChallenge.create("Bearer", realm));
			
		return path("authentication", () -> route(
				post(() -> entity(Jackson.unmarshaller(User.class), user -> {
					String userquery = "SELECT * FROM Users where name = $1 and pw = $2 limit 1";
		    		LOGGER.info(this.getClass().getName()
		    				.concat(" ")
		    				.concat(req.http.method().name())
		    				.concat(" (from uid: ")
		    				.concat(req.uid.toString())
		    				.concat(" ) : ")
		    				.concat(req.http.getUri().toString())
		    				);
					Observable<String> res =
						pool.queryRows(userquery, user.getName(), user.getPw())
						.map(result -> {
							User u = new User(result);
							try {
								String ret = encodetoken(u);
								LOGGER.info(this.getClass().getName()
					    				.concat(" ")
					    				.concat(req.http.method().name())
					    				.concat(" (from uid: ")
					    				.concat(u.getId().toString())
					    				.concat(" ) : ")
					    				.concat(req.http.getUri().toString())
					    				.concat(" SUCCESS")
					    				);
								return ret;
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return null;
						})
						.take(1);
					//CompletableFuture<String> f = Futures.fromObservable(res);
					//Single<String> s = res.toSingle();
					//return s.doOnSuccess((token) -> complete(token));
					//CompletionStage<HttpResponse> result;
					//subscriber = RxReactiveStreams.toSubscriber(complete());
					//RxReactiveStreams.toPublisher(res).subscribe(subscriber);
					//return res.doOnRequest(complete("OK"));
					//return completeWithFuture(res);
					// FIXME: Don't do blocking request here.
					return complete(res.toBlocking().single());
					//return completeOKWithSource(Source.fromPublisher(RxReactiveStreams.toPublisher(res)),
					//		Jackson.marshaller(), EntityStreamingSupport.json());
					
					
				}))
				//,get (() -> respondWithHeader(auth, () -> complete("Please login to access your resources")))
				)
			);
	}
}
