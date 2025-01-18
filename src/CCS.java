import Logs.Dependencies.Site;
import Exceptions.FlagException;
import Exceptions.PortException;
import Logs.Logger;
import Service.ServerCCS;

public class CCS {

    public static void main(String[] args) {
        Logger.setPrefix(Site.SERVER);
        try {
            if (args.length != 1)
                throw new FlagException("Incorrect number of flags.\n" +
                    "Syntax is: java -jar CCS.jar <port>");
            int port = getPort(args[0]);
            ServerCCS server = new ServerCCS(port);
            server.run();
        } catch (FlagException | PortException exception) {
            System.err.println(" Input values exception:\n" + exception.getMessage());
        } catch (Exception exception) {
            System.err.println(" Server running exception:\n" + exception.getMessage());
        }
    }

    public static int getPort(String text) throws PortException {
        if (text == null || text.isEmpty())
            throw new PortException("Empty argument.");
        int port;
        try { port = Integer.parseInt(text);
        } catch (NumberFormatException nfe) {
            throw new PortException(
                "The port specified is not the integer size number.");
        }
        if (port < 1 || port > 65535)
            throw new PortException("Invalid port number. Available are <0-65535>.");
        return port;
    }

}