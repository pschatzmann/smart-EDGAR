package ch.pschatzmann.edgar.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

/**
 * Simple file server which is used to serve the swagger files
 * 
 * @author pschatzmann
 *
 */
@Path("/edgar")
public class SwaggerEdgarService extends SwaggerService {
	protected String getIndex() {
		return "/edgar/index.html";
	}
	
	protected String getFilePath(String fileName) {
		return fileName.replace("/edgar","");
	}


}
