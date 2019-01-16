package ch.pschatzmann.edgar.service;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Allow cors requests
 * @author pschatzmann
 *
 */
public class CORSResponseFilter implements ContainerResponseFilter {

	/**
	 * Add additional headers to the response context
	 */
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		MultivaluedMap<String, Object> headers = responseContext.getHeaders();

		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
		// headers.add("Access-Control-Allow-Headers", "X-Requested-With,
		// Content-Type, X-Codingpedia");
	}

}