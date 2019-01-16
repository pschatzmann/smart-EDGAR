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
@Path("/")
public class SwaggerService {
	private final static Logger LOG = Logger.getLogger(SwaggerService.class);

    @GET
    public Response root() {
      return Response.seeOther(URI.create("/index.html")).build();
    }
	
	@GET
	@Path("/{fileName}")
	public Response swagger(@PathParam("fileName")  String fileName) throws IOException, Exception {
		String type = getContentType(fileName);
		InputStream is = this.getClass().getResourceAsStream(("/swagger/"+getFilePath(fileName)));
		
		// if the resource does not exist we forward to the index
		if (is==null) {
		     return Response.seeOther(URI.create(getIndex())).build();
		}
		
	    return Response.ok(is).type(type).build();
	}


	private String getContentType(String fileName) {
		if (fileName.endsWith(".html")) {
			return "text/html";
		} 
		if (fileName.endsWith(".css")) {
			return "text/css";
		}
		if (fileName.endsWith(".js")) {
			return "text/javascript";
		}
		if (fileName.endsWith(".png")) {
			return "image/png";
		}
		if (fileName.endsWith(".yaml")) {
			return "application/yaml";
		}
		return "text/html";
	}
	
	protected String getIndex() {
		return "/index.html";
	}

	protected String getFilePath(String fileName) {
		return fileName;
	}


}
