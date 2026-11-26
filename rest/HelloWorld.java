package firstREST;

import org.restlet.data.Protocol;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.Server;


public class HelloWorld extends ServerResource {

    public static void main(String[] args) throws Exception {
        new Server(Protocol.HTTP, 8182, HelloWorld.class).start();
    }

    @Get
    public String present() {
        return "hello, world";
    }
}
