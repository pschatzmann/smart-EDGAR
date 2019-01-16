package ch.pschatzmann.edgar.service;

import java.net.URI;
import java.net.UnknownHostException;

import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import ch.pschatzmann.common.utils.Utils;


/**
 * Startup of the webservice on localhost. The port can be passed as parameter. If no port is
 * indicated we use port 9997.
 * 
 * @author pschatzmann
 *
 */
public class Main {
	private final static Logger LOG = Logger.getLogger(Main.class);
	/**
	 * Constructor
	 */
	public Main() {		
	}
	/**
	 * Starts the service server
	 * @param args
	 * @throws UnknownHostException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		String port = "9997";
		String host = "0.0.0.0";
		if (args.length > 0) {
			host = args[0];
		}
		if (args.length > 1) {
			port = args[1];
		}

		String url = "http://" + host+"/";

		URI baseUri = UriBuilder.fromUri(url).port(Integer.parseInt(port)).build();
		ResourceConfig config = new ResourceConfig();
        config.property(ServerProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, 200);

        config.register(JacksonFeature.class);
		config.register(EdgarDBService.class);
		config.register(EdgarFileService.class);
		config.register(SwaggerService.class);
		config.register(SwaggerEdgarService.class);
		config.register(MultiPartFeature.class);
		config.register(GenericExceptionMapper.class);
		config.register(CORSResponseFilter.class);		


		JdkHttpServerFactory.createHttpServer(baseUri, config);
		LOG.info("HTTP Server started on " + url.substring(0, url.length()-1) + ":" + port);
		Utils.waitForever();
	}
	
}
