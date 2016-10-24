package uk.ac.lancs.ucrel.handler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("lexidb")
public class Query {

    @GET
    @Path("kwic")
    @Produces(MediaType.TEXT_PLAIN)
    public String kwic(){
        return "kwic result";
    }

}
