package uk.ac.lancs.ucrel.handler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.cmd.Command;
import uk.ac.lancs.ucrel.server.ServerImpl;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("lexidb")
public class Query {

    private Map<String, Command> commands;
    private DefaultParser parser = new DefaultParser();
    private static Logger LOG = Logger.getLogger(Query.class);

    public Query(){
        commands = new HashMap<String, Command>();
        for (Command c : Command.getDefaultCommands(ServerImpl.getInstance())) {
            commands.put(c.getUsage().split(" ")[0], c);
        }
    }

    @GET
    @Path("query")
    @Produces(MediaType.APPLICATION_JSON)
    public Response kwic(@QueryParam("q") String query){
        LOG.info("Query received: " + query);
        try {
            CommandLine line = parser.parse(new Options(), query.split(" "), true);
            String op = line.getArgs()[0];
            if (commands.containsKey(op)) {
                Command c = commands.get(op);
                line = getCommandLine(c, query);
                c.invoke(line);
                return Response.ok().header("Access-Control-Allow-Origin", "*").entity(c.json()).build();
            } else {
                System.err.println("Command not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Command failed!: " + e.getMessage());
        }
        return null;
    }

    private CommandLine getCommandLine(Command c, String cmd) {
        try {
            return parser.parse(c.getOptions(), cmd.split(" "));
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            c.printHelp();
            return null;
        }
    }
}
