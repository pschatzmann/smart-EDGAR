package ch.pschatzmann.edgar.service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import ch.pschatzmann.edgar.base.errors.ErrorInformation;


/**
 * REST ExceptionMapper which provides the Exception information as json to the caller
 * 
 * @author pschatzmann
 *
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {	
	private static Logger LOG = Logger.getLogger(GenericExceptionMapper.class);
	
	public GenericExceptionMapper() {}
	
	@Override
	public Response toResponse(Throwable ex) {
		LOG.error(ex,ex);
		ErrorInformation ei = new ErrorInformation(400, ex.getMessage());
    		return Response.status(ei.getStatus())
    				.entity(ei)
    				.type(MediaType.APPLICATION_JSON).
    				build();
    	}


}

