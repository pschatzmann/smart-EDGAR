package ch.pschatzmann.edgar.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
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
		return Response.seeOther(URI.create(getIndex())).build();
	}

	@GET
	@Path("/{fileName}")
	public Response swagger(@PathParam("fileName") String fileName) throws IOException, Exception {
		LOG.info(fileName);
		String type = getContentType(fileName);
		InputStream is = this.getClass().getResourceAsStream(("/swagger/" + getFilePath(fileName)));

		// if the resource does not exist we forward to the index
		if (is == null) {
			return Response.seeOther(URI.create(getIndex())).build();
		}

		StreamingOutput so = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException {
				IOUtils.copy(is, os);
				os.flush();
				is.close();
				LOG.info(fileName+" -> Done");
			}
		};

		CacheControl cc = new CacheControl();
		// 1 year
		cc.setMaxAge(31536000);
		return Response.ok(so).type(type).cacheControl(cc).build();
	}

	private String getContentType(String fileName) {
		if (fileName.endsWith(".html")) {
			return "text/html";
		}
		if (fileName.endsWith(".css")) {
			return "text/css";
		}
		if (fileName.endsWith(".js")) {
			return "application/javascript";
		}
		if (fileName.endsWith(".png")) {
			return "image/png";
		}
		if (fileName.endsWith(".yaml")) {
			return "application/yaml";
		}
		if (fileName.endsWith(".map")) {
			return "application/octet-stream";
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
