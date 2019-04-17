package ch.pschatzmann.edgar.service;

import javax.ws.rs.Path;

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
