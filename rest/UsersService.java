package rest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
public class UsersService {

    private static final Map<Integer, String> USERS = new ConcurrentHashMap<>();

    static {
        USERS.put(1, "Alice");
        USERS.put(2, "Bob");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers() {
        StringBuilder json = new StringBuilder("[");
        int i = 1;
        for (Map.Entry<Integer, String> u : USERS.entrySet()) {
            if (i++ > 1) json.append(", ");
            json.append("{").append("\"id\": ").append(u.getKey())
                .append(", \"nom\": \"").append(u.getValue()).append("\"}");
        }
        return Response.ok(json.append("]").toString()).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") int id) {
        String nom = USERS.get(id);
        if (nom == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok("{\"id\": " + id + ", \"nom\": \"" + nom + "\"}").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(String body) {
        String nom = body.replaceAll(".*\"nom\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        int nouvelId = USERS.size() + 1;
        USERS.put(nouvelId, nom);

        return Response.status(Response.Status.CREATED)
                       .entity("{\"id\": " + nouvelId + ", \"nom\": \"" + nom + "\"}")
                       .build();
    }
}
