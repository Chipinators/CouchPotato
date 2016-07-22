package paperprisoners.couchpotato;

/**
 * Created by Chris on 7/20/2016.
 */
public interface MessageListener {

    public abstract void onReceiveMessage(int player, int messageType, Object[] content);
}
