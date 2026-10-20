package jade.core.behaviours;

import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.List;

/**
 * The two typed maps used by a behaviour (and by the states of a protocol) to exchange messages :
 * <ul>
 *     <li>{@link #lists()} : key -> list of messages (ex. all the responses received by an initiator)</li>
 *     <li>{@link #messages()} : key -> one message (ex. the reply that a responder must send)</li>
 * </ul>
 * They are kept TOGETHER in one object, so that sharing this object between behaviours
 * ({@link Behaviour#setMessageMaps(MessageMaps)}) always shares both maps : it is no longer possible
 * to share one map and forget the other (this replaces the untyped DataStore of the original JADE).
 * <p>
 * The maps are created lazily, the first time they are requested : all the behaviours sharing this
 * object then use the SAME maps, even if none was given at construction.
 */
public final class MessageMaps {
    private HashMap<String, List<ACLMessage>> lists;
    private HashMap<String, ACLMessage> messages;

    /** empty maps, created on first use */
    public MessageMaps() {
    }

    /**
     * @param lists    the map key -> list of messages to use (if null, created on first use)
     * @param messages the map key -> message to use (if null, created on first use)
     */
    public MessageMaps(HashMap<String, List<ACLMessage>> lists, HashMap<String, ACLMessage> messages) {
        this.lists = lists;
        this.messages = messages;
    }

    /** the map key -> list of messages (created if needed) */
    public synchronized HashMap<String, List<ACLMessage>> lists() {
        if (lists == null) lists = new HashMap<>();
        return lists;
    }

    /** the map key -> message (created if needed) */
    public synchronized HashMap<String, ACLMessage> messages() {
        if (messages == null) messages = new HashMap<>();
        return messages;
    }

    /** a new MessageMaps object with another map of lists and the SAME map of messages as this one */
    MessageMaps withLists(HashMap<String, List<ACLMessage>> otherLists) {
        return new MessageMaps(otherLists, messages());
    }

    /** a new MessageMaps object with the SAME map of lists as this one and another map of messages */
    MessageMaps withMessages(HashMap<String, ACLMessage> otherMessages) {
        return new MessageMaps(lists(), otherMessages);
    }
}
