/* Generated By:JavaCC: Do not edit this line. ACLParser.java */
package jade.lang.acl;

import jade.core.AID;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

/**
 * Javadoc documentation for the file
 *
 * @author Fabio Bellifemine - CSELT S.p.A
 * @version $Date: 2005-04-15 17:50:12 +0200 (Fri, 15 Apr 2005) $ $Revision: 5671 $
 */

public class ACLParser implements ACLParserConstants {
    static private int[] jj_la1_0;
    static private int[] jj_la1_1;

    static {
        jj_la1_0();
        jj_la1_1();
    }

    final private int[] jj_la1 = new int[14];
    private final Vector<int[]> jj_expentries = new Vector<>();
    public ACLParserTokenManager token_source;
    public Token token, jj_nt;
    ACLMessage msg = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
    SimpleCharStream jj_input_stream;
    private int jj_ntk;
    private int jj_gen;
    private int[] jj_expentry;
    private int jj_kind = -1;

    public ACLParser(InputStream stream) {
        jj_input_stream = new SimpleCharStream(stream, 1, 1);
        token_source = new ACLParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    }

    public ACLParser(Reader stream) {
        jj_input_stream = new SimpleCharStream(stream, 1, 1);
        token_source = new ACLParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    }

    public ACLParser(ACLParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    }

    public static void main(String[] args) throws ParseException {
        ACLParser parser = new ACLParser(System.in);

        while (true) {
            try {
                ACLMessage result = parser.Message();
                System.out.println(result);
            } catch (ParseException pe) {
                pe.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static ACLParser create() {
        Reader r = new StringReader("");
        return new ACLParser(r);
    }

    private static void jj_la1_0() {
        jj_la1_0 = new int[]{0xfff8000, 0x0, 0x0, 0xfff8000, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,};
    }

    private static void jj_la1_1() {
        jj_la1_1 = new int[]{0x0, 0x800000, 0x800000, 0x0, 0x10c, 0x4, 0x800000, 0x3c0000, 0x3c0000, 0x5fe, 0x5fe, 0x108, 0xf0, 0xc0,};
    }

    public ACLMessage parse(Reader text) throws ParseException {
        ReInit(text);
        return Message();
    }

    public AID parseAID(Reader text) throws ParseException {
        if (text != null) {
            ReInit(text);
        }

        token_source.SwitchTo(AIDSTATE);
        AID result = AgentIdentifier();
        token_source.SwitchTo(DEFAULT);
        return result;
    }

    private String trimQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && (s.endsWith("\"")))
            s = s.substring(1, s.length() - 1);
        return unescape(s);
    }

    private String unescape(String s) {
        StringBuilder result = new StringBuilder(s.length());
        int i;
        for (i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) == '\\' && s.charAt(i + 1) == '\"') {
                result.append("\"");
                i++;
            } else {
                result.append(s.charAt(i));
            }
        }
        // NOTE: if s terminates with \" (this is the case when i == s.length()) the
        // last character should not be appended as it has already been considered.
        if (i < s.length()) {
            result.append(s.charAt(s.length() - 1));
        }
        return result.toString();
    }

    final public ACLMessage Message() throws ParseException {
        msg.reset();
        jj_consume_token(START);
        MessageType();
        label_1:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case SENDER:
                case RECEIVER:
                case CONTENT:
                case REPLY_WITH:
                case REPLY_BY:
                case IN_REPLY_TO:
                case REPLY_TO:
                case ENCODING:
                case LANGUAGE:
                case ONTOLOGY:
                case PROTOCOL:
                case CONVERSATION_ID:
                case USERDEFINEDPARAM:
                    break;
                default:
                    jj_la1[0] = jj_gen;
                    break label_1;
            }
            MessageParameter();
        }
        jj_consume_token(END);
        {
            if (true) return msg;
        }
        throw new Error("Missing return statement in function");
    }

    final public void MessageType() throws ParseException {
        Token t;
        t = jj_consume_token(MESSAGETYPE);
        msg.setPerformative(ACLMessage.getInteger(t.image));
    }

    final public void MessageParameter() throws ParseException {
        String s;
        Token t;
        AID aid;
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case SENDER -> {
                jj_consume_token(SENDER);
                aid = AgentIdentifier();
                msg.setSender(aid);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case RECEIVER -> {
                jj_consume_token(RECEIVER);
                msg.clearAllReceiver();
                jj_consume_token(LBRACE2);
                jj_consume_token(SET);
                label_2:
                while (true) {
                    if (((jj_ntk == -1) ? jj_ntk() : jj_ntk) == LBRACE2) {
                    } else {
                        jj_la1[1] = jj_gen;
                        break;
                    }
                    aid = AgentIdentifier();
                    msg.addReceiver(aid);
                }
                jj_consume_token(RBRACE2);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case CONTENT -> {
                jj_consume_token(CONTENT);
                s = Content();
                msg.setContent(s);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case REPLY_WITH -> {
                jj_consume_token(REPLY_WITH);
                s = Expression();
                msg.setReplyWith(s);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case REPLY_BY -> {
                jj_consume_token(REPLY_BY);
                s = DateTimeToken();
                try {
                    msg.setReplyByDate(ISO8601.toDate(s));
                } catch (Exception ignored) {
                }
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case IN_REPLY_TO -> {
                jj_consume_token(IN_REPLY_TO);
                s = Expression();
                msg.setInReplyTo(s);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case REPLY_TO -> {
                jj_consume_token(REPLY_TO);
                msg.clearAllReplyTo();
                jj_consume_token(LBRACE2);
                jj_consume_token(SET);
                label_3:
                while (true) {
                    if (((jj_ntk == -1) ? jj_ntk() : jj_ntk) == LBRACE2) {
                    } else {
                        jj_la1[2] = jj_gen;
                        break;
                    }
                    aid = AgentIdentifier();
                    msg.addReplyTo(aid);
                }
                jj_consume_token(RBRACE2);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case ENCODING -> {
                jj_consume_token(ENCODING);
                s = Expression();
                msg.setEncoding(s);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case LANGUAGE -> {
                jj_consume_token(LANGUAGE);
                s = Expression();
                msg.setLanguage(s);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case ONTOLOGY -> {
                jj_consume_token(ONTOLOGY);
                s = Expression();
                msg.setOntology(s);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case PROTOCOL -> {
                jj_consume_token(PROTOCOL);
                s = Word();
                msg.setProtocol(s);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case CONVERSATION_ID -> {
                jj_consume_token(CONVERSATION_ID);
                s = Expression();
                msg.setConversationId(s);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            case USERDEFINEDPARAM -> {
                t = jj_consume_token(USERDEFINEDPARAM);
                s = Expression();
                msg.addUserDefinedParameter(t.image.substring(3), s);
                token_source.SwitchTo(MESSAGEPARAMETERSTATE);
            }
            default -> {
                jj_la1[3] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    final public String Content() throws ParseException {
        String s;
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case STRINGLITERAL, PREFIXBYTELENGTHENCODEDSTRING -> {
                s = Stringa();
                {
                    if (true) return s;
                }
            }
            case WORD -> {
                s = Word();
                {
                    if (true) return s;
                }
            }
            default -> {
                jj_la1[4] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
            }
        }
        throw new Error("Missing return statement in function");
    }

    final public AID AgentIdentifier() throws ParseException {
        Token t;
        String s;
        AID aid;
        AID cur = new AID();
        jj_consume_token(LBRACE2);
        jj_consume_token(AID);
        label_4:
        while (true) {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case NAME -> {
                    jj_consume_token(NAME);
                    s = Content();
                    cur.setName(s);
                    token_source.SwitchTo(AIDSTATE);
                }
                case ADDRESSES -> {
                    jj_consume_token(ADDRESSES);
                    jj_consume_token(LBRACE2);
                    jj_consume_token(SEQUENCE);
                    token_source.SwitchTo(CONTENTSTATE);
                    label_5:
                    while (true) {
                        if (((jj_ntk == -1) ? jj_ntk() : jj_ntk) == WORD) {
                        } else {
                            jj_la1[5] = jj_gen;
                            break;
                        }
                        s = Word();
                        cur.addAddresses(s);
                    }
                    jj_consume_token(RBRACE);
                    token_source.SwitchTo(AIDSTATE);
                }
                case RESOLVERS -> {
                    jj_consume_token(RESOLVERS);
                    jj_consume_token(LBRACE2);
                    jj_consume_token(SEQUENCE);
                    label_6:
                    while (true) {
                        if (((jj_ntk == -1) ? jj_ntk() : jj_ntk) == LBRACE2) {
                        } else {
                            jj_la1[6] = jj_gen;
                            break;
                        }
                        aid = AgentIdentifier();
                        cur.addResolvers(aid);
                    }
                    jj_consume_token(RBRACE2);
                    token_source.SwitchTo(AIDSTATE);
                }
                case USERDEFINEDSLOT -> {
                    t = jj_consume_token(USERDEFINEDSLOT);
                    s = Expression();
                    cur.addUserDefinedSlot(t.image.substring(3), s);
                    token_source.SwitchTo(AIDSTATE);
                }
                default -> {
                    jj_la1[7] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
                }
            }
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case NAME:
                case ADDRESSES:
                case RESOLVERS:
                case USERDEFINEDSLOT:
                    break;
                default:
                    jj_la1[8] = jj_gen;
                    break label_4;
            }
        }
        jj_consume_token(RBRACE2);
        {
            if (true) return cur;
        }
        throw new Error("Missing return statement in function");
    }

    final public String Expression() throws ParseException {
        String s;
        StringBuilder s1 = new StringBuilder();
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case WORD -> {
                s = Word();
                {
                    if (true) return s;
                }
            }
            case STRINGLITERAL, PREFIXBYTELENGTHENCODEDSTRING -> {
                s = Stringa();
                {
                    if (true) return s;
                }
            }
            case DIGIT, INTEGER, FLOATONE, FLOATTWO -> {
                s = Number();
                {
                    if (true) return s;
                }
            }
            case DATETIME -> {
                s = DateTimeToken();
                {
                    if (true) return s;
                }
            }
            case LBRACE -> {
                jj_consume_token(LBRACE);
                label_7:
                while (true) {
                    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                        case DATETIME:
                        case WORD:
                        case STRINGLITERAL:
                        case DIGIT:
                        case INTEGER:
                        case FLOATONE:
                        case FLOATTWO:
                        case PREFIXBYTELENGTHENCODEDSTRING:
                        case LBRACE:
                            break;
                        default:
                            jj_la1[9] = jj_gen;
                            break label_7;
                    }
                    s = Expression();
                    s1.append(s).append(" ");
                }
                jj_consume_token(RBRACE);
                {
                    if (true) return "(" + s1 + ")";
                }
            }
            default -> {
                jj_la1[10] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
            }
        }
        throw new Error("Missing return statement in function");
    }

    final public String Word() throws ParseException {
        Token t;
        t = jj_consume_token(WORD);
        {
            if (true) return trimQuotes(t.image);
        }
        throw new Error("Missing return statement in function");
    }

    final public String Stringa() throws ParseException {
        String s;
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case STRINGLITERAL -> {
                s = StringLiteral();
                {
                    if (true) return s;
                }
            }
            case PREFIXBYTELENGTHENCODEDSTRING -> {
                s = ByteLengthEncodedString();
                {
                    if (true) return s;
                }
            }
            default -> {
                jj_la1[11] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
            }
        }
        throw new Error("Missing return statement in function");
    }

    final public String StringLiteral() throws ParseException {
        Token t;
        t = jj_consume_token(STRINGLITERAL);
        {
            if (true) return trimQuotes(t.image);
        }
        throw new Error("Missing return statement in function");
    }

    final public String ByteLengthEncodedString() throws ParseException {
        Token t;
        t = jj_consume_token(PREFIXBYTELENGTHENCODEDSTRING);
        {
            if (true) return t.image;
        }
        throw new Error("Missing return statement in function");
    }

    final public String Number() throws ParseException {
        String s;
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case DIGIT -> {
                s = Digit();
                {
                    if (true) return s;
                }
            }
            case INTEGER -> {
                s = Integer();
                {
                    if (true) return s;
                }
            }
            case FLOATONE, FLOATTWO -> {
                s = Float();
                {
                    if (true) return s;
                }
            }
            default -> {
                jj_la1[12] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
            }
        }
        throw new Error("Missing return statement in function");
    }

    final public String DateTimeToken() throws ParseException {
        Token t;
        String s;
        t = jj_consume_token(DATETIME);
        {
            if (true) return t.image;
        }
        throw new Error("Missing return statement in function");
    }

    final public String Digit() throws ParseException {
        Token t;
        t = jj_consume_token(DIGIT);
        {
            if (true) return t.image;
        }
        throw new Error("Missing return statement in function");
    }

    final public String Integer() throws ParseException {
        Token t;
        String s = "";
        // (t="+" {s+=t.image;} | t="-" {s+=t.image;})? ( t=<DIGIT> {s+=t.image;} )+	{return s;}*/
        t = jj_consume_token(INTEGER);
        {
            if (true) return t.image;
        }
        throw new Error("Missing return statement in function");
    }

    final public String Float() throws ParseException {
        Token t;
        switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
            case FLOATONE -> {
                t = jj_consume_token(FLOATONE);
                {
                    if (true) return t.image;
                }
            }
            case FLOATTWO -> {
                t = jj_consume_token(FLOATTWO);
                {
                    if (true) return t.image;
                }
            }
            default -> {
                jj_la1[13] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
            }
        }
        throw new Error("Missing return statement in function");
    }

    public void ReInit(InputStream stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    }

    public void ReInit(Reader stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    }

    public void ReInit(ACLParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 14; i++) jj_la1[i] = -1;
    }

    private Token jj_consume_token(int kind) throws ParseException {
        Token oldToken;
        if ((oldToken = token).next != null) token = token.next;
        else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        if (token.kind == kind) {
            jj_gen++;
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }

    final public Token getNextToken() {
        if (token.next != null) token = token.next;
        else token = token.next = token_source.getNextToken();
        jj_ntk = -1;
        jj_gen++;
        return token;
    }

    final public Token getToken(int index) {
        Token t = token;
        for (int i = 0; i < index; i++) {
            if (t.next != null) t = t.next;
            else t = t.next = token_source.getNextToken();
        }
        return t;
    }

    private int jj_ntk() {
        if ((jj_nt = token.next) == null)
            return (jj_ntk = (token.next = token_source.getNextToken()).kind);
        else
            return (jj_ntk = jj_nt.kind);
    }

    public ParseException generateParseException() {
        jj_expentries.removeAllElements();
        boolean[] la1tokens = new boolean[56];
        for (int i = 0; i < 56; i++) {
            la1tokens[i] = false;
        }
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 14; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                    if ((jj_la1_1[i] & (1 << j)) != 0) {
                        la1tokens[32 + j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 56; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.addElement(jj_expentry);
            }
        }
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = jj_expentries.elementAt(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }

    final public void enable_tracing() {
    }

    final public void disable_tracing() {
    }

}
