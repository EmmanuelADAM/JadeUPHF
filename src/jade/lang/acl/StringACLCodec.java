/*
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.lang.acl;

//#MIDP_EXCLUDE_FILE

import jade.core.AID;
import jade.core.CaseInsensitiveString;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 * This class implements the FIPA String codec for ACLMessages.
 * Notice that it is not possible to convey
 * a sequence of bytes over a StringACLCodec because the bytes with
 * the 8th bit ON cannot properly converted into a char.
 *
 * @author Fabio Bellifemine - CSELT S.p.A.
 * @author Nicolas Lhuillier - Motorola
 * @version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $
 **/
public class StringACLCodec implements ACLCodec {

    /**
     * String constant for the name of the ACL representation managed
     * by this ACL codec.
     */
    public static final String NAME = jade.domain.FIPANames.ACLCodec.STRING;

    /**
     * Key of the user-defined parameter used to signal the automatic JADE
     * conversion of the content into Base64 encoding
     **/
    private static final String BASE64ENCODING_KEY = "JADE-Encoding";
    /**
     * Value of the user-defined parameter used to signal the automatic JADE
     * conversion of the content into Base64 encoding
     **/
    private static final String BASE64ENCODING_VALUE = "Base64";

    private static final String SENDER = " :sender ";
    private static final String RECEIVER = " :receiver ";
    private static final String CONTENT = " :content ";
    private static final String REPLY_WITH = " :reply-with ";
    private static final String IN_REPLY_TO = " :in-reply-to ";
    private static final String REPLY_TO = " :reply-to ";
    private static final String LANGUAGE = " :language ";
    private static final String ENCODING = " :encoding ";
    private static final String ONTOLOGY = " :ontology ";
    private static final String REPLY_BY = " :reply-by ";
    private static final String PROTOCOL = " :protocol ";
    private static final String CONVERSATION_ID = " :conversation-id ";

    ACLParser parser = null;
    Writer out = null;

    /**
     * constructor for the codec.
     * The standard input is used as an input stream of ACL messages.
     * The standard output is used to write encoded ACL messages.
     */
    public StringACLCodec() {
        //parser = new ACLParser(System.in);
        //out = new OutputStreamWriter(System.out);
    }


    /**
     * constructor for the codec.
     *
     * @parameter r is the input stream for the ACL Parser (pass
     * <code>new InputStreamReader(System.in)</code>
     * if you want to use the standard input)
     * @parameter w is the writer to write encoded ACL messages (pass
     * <code>new OutputStreamWriter(System.out)</code> if you want to
     * use the standard output)
     */
    public StringACLCodec(Reader r, Writer w) {
        parser = new ACLParser(r);
        out = w;
    }


    /**
     * if there was an automatical Base64 encoding, then it performs
     * automatic decoding.
     **/
    private void checkBase64Encoding(ACLMessage msg) {
        String encoding = msg.getUserDefinedParameter(BASE64ENCODING_KEY);
        if (CaseInsensitiveString.equalsIgnoreCase(BASE64ENCODING_VALUE, encoding)) {
            try { // decode Base64
                String content = msg.getContent();
                if ((content != null) && (content.length() > 0)) {
                    msg.setByteSequenceContent(Base64.decodeBase64(content.getBytes(StandardCharsets.US_ASCII)));
                    msg.removeUserDefinedParameter(BASE64ENCODING_KEY); // reset the slot value for encoding
                }
            } catch (StringIndexOutOfBoundsException | NullPointerException e) {
                e.printStackTrace();
            } catch (NoClassDefFoundError jlncdfe) {
                System.err.println("\t\t===== E R R O R !!! =======\n");
                System.err.println("Missing support for Base64 conversions");
                System.err.println("Please refer to the documentation for details.");
                System.err.println("=============================================\n\n");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
            }
        } //end of if CaseInsensitiveString
    }

    /**
     * decode and parses the next message from the Reader passed in the
     * constructor.
     *
     * @return the ACLMessage
     * @throws CodecException if any Exception occurs during the
     *                        parsing/reading operation
     */
    public ACLMessage decode() throws CodecException {
        try {
            ACLMessage msg = parser.Message();
            checkBase64Encoding(msg);
            return msg;
        } catch (TokenMgrError e1) {
            throw new CodecException(getName() + " ACLMessage decoding token exception", e1);
        } catch (Exception e) {
            throw new CodecException(getName() + " ACLMessage decoding exception", e);
        }
    }

    /**
     * Parse an agent identifier, without it being included within an
     * ACL message.
     */
    public AID decodeAID() throws CodecException {
        try {
            return parser.parseAID(null);
        } catch (TokenMgrError e1) {
            throw new CodecException(getName() + " AID decoding token exception", e1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CodecException(getName() + " AID decoding exception", e);
        }
    }

    /**
     * encodes the message and writes it into the Writer passed in the
     * constructor.
     * Notice that this method does not call <code>flush</code> on the writer.
     *
     * @ param msg is the ACLMessage to encode and write into
     */
    public void write(ACLMessage msg) {
        try {
            out.write(toString(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static private String escape(String s) {
        // Make the stringBuffer a little larger than strictly
        // necessary in case we need to insert any additional
        // characters.  (If our size estimate is wrong, the
        // StringBuffer will automatically grow as needed).
        StringBuilder result = new StringBuilder(s.length() + 20);
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == '"')
                result.append("\\\"");
            else
                result.append(s.charAt(i));
        return result.toString();
    }

    /**
     * Take a java String and quote it to form a legal FIPA ACL string.
     * Add quotation marks to the beginning/end and escape any
     * quotation marks inside the string.
     */
    static private String quotedString(String str) {
        return "\"" + escape(str) + "\"";
    }

    /**
     * If a user-defined parameter contain a blank char inside, then it is skipped for FIPA-compatibility
     *
     * @return a String encoded message
     * @see ACLMessage#toString()
     **/
    static String toString(ACLMessage msg) {
        StringBuffer str = new StringBuffer("(");
        str.append(ACLMessage.getPerformative(msg.getPerformative())).append("\n");
        AID sender = msg.getSender();
        if (sender != null)
            str.append(SENDER + " ").append(sender).append("\n");
        Iterator<AID> it = msg.getAllReceiver();
        if (it.hasNext()) {
            str.append(RECEIVER + " (set ");
            while (it.hasNext())
                str.append(it.next().toString()).append(" ");
            str.append(")\n");
        }
        it = msg.getAllReplyTo();
        if (it.hasNext()) {
            str.append(REPLY_TO + " (set \n");
            while (it.hasNext())
                str.append(it.next().toString()).append(" ");
            str.append(")\n");
        }
        if (msg.hasByteSequenceContent()) {
            str.append(":X-" + BASE64ENCODING_KEY + " " + BASE64ENCODING_VALUE + "\n");
            try {
                String b64 = new String(Base64.encodeBase64(msg.getByteSequenceContent()), StandardCharsets.US_ASCII);
                str.append(CONTENT + " \"").append(b64).append("\" \n");
            } catch (NoClassDefFoundError jlncdfe) {
                System.err.println("\n\t===== E R R O R !!! =======\n");
                System.err.println("Missing support for Base64 conversions");
                System.err.println("Please refer to the documentation for details.");
                System.err.println("=============================================\n\n");
                System.err.println();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
            }
        } else {
            String content = msg.getContent();
            if (content != null) {
                content = content.trim();
                if (content.length() > 0)
                    str.append(CONTENT + " \"").append(escape(content)).append("\" \n");
            }
        }
        appendACLExpression(str, REPLY_WITH, msg.getReplyWith());
        appendACLExpression(str, IN_REPLY_TO, msg.getInReplyTo());
        appendACLExpression(str, ENCODING, msg.getEncoding());
        appendACLExpression(str, LANGUAGE, msg.getLanguage());
        appendACLExpression(str, ONTOLOGY, msg.getOntology());

        Date d = msg.getReplyByDate();
        if (d != null)
            str.append(REPLY_BY + " ").append(ISO8601.toString(d)).append("\n");

        String tmp = msg.getProtocol();
        if (tmp != null) {
            tmp = tmp.trim();
            if (tmp.length() > 0)
                str.append(PROTOCOL + " ").append(tmp).append("\n");
        }

        appendACLExpression(str, CONVERSATION_ID, msg.getConversationId());

        Properties userDefProps = msg.getAllUserDefinedParameters();
        if (userDefProps != null) {
            Enumeration<?> e = userDefProps.propertyNames();
            while (e.hasMoreElements()) {
                String key = ((String) e.nextElement());
                if (key.indexOf(' ') == -1) {
                    if ((!key.startsWith("X-")) && (!key.startsWith("x-")))
                        appendACLExpression(str, ":X-" + key, userDefProps.getProperty(key));
                    else
                        appendACLExpression(str, ":" + key, userDefProps.getProperty(key));
                } else
                    System.err.println("WARNING: The slotName of user-defined parameters cannot contain blanks inside. Therefore " + key + " is not being encoded");
            }
        }
        str.append(")");

        return str.toString();
    }

    /**
     * If the content of the message is a byteSequence, then this
     * method encodes the content in Base64 and automatically sets the value
     * of the encoding slot.
     * see ACLCodec#encode(ACLMessage msg)
     */
    public byte[] encode(ACLMessage msg, String charset) {
        try {
            return toString(msg).getBytes(charset);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return new byte[0];
        }
    }


    /**
     * see ACLCodec#decode(byte[] data)
     */
    public ACLMessage decode(byte[] data, String charset) throws CodecException {
        try {
            ACLMessage msg = ACLParser.create().parse(new InputStreamReader(new ByteArrayInputStream(data), charset));
            checkBase64Encoding(msg);
            return msg;
        } catch (TokenMgrError e1) {
            throw new CodecException(getName() + " ACLMessage decoding token exception", e1);
        } catch (Exception e2) {
            throw new CodecException(getName() + " ACLMessage decoding exception", e2);
        }
    }

    /**
     * @return the name of this encoding according to the FIPA specifications
     */
    public String getName() {
        return NAME;
    }

    /**
     * append to the passed StringBuffer the slot name and value separated
     * by a blank char and followed by a newline.
     * If the value contains a blank, then it is quoted.
     * if the value is null or its length is zero, the method does nothing.
     **/
    static public void appendACLExpression(StringBuffer str, String slotName, String slotValue) {
        if ((slotValue != null) && (slotValue.length() > 0)) {
            if (!isAWord(slotValue)) {
                try {
                    // if the value is a number, then leave as it is
                    Double.valueOf(slotValue);
                } catch (NumberFormatException e) {
                    // if the program is here, then slotValue is neither a
                    // word or a number. Therefore it must be quoted
                    slotValue = quotedString(slotValue);
                }
            }
            str.append(slotName).append(" ").append(slotValue).append(" ");
        }
    }


    private static final String illegalFirstChar = "#0123456789-";

    /**
     * Test if the given string is a legal word using the FIPA ACL spec.
     * In addition to FIPA's restrictions, place the additional restriction
     * that a Word can not contain a '\"', that would confuse the parser at
     * the other end.
     */
    static private boolean isAWord(String s) {
        // This should permit strings of length 0 to be encoded.
        if (s == null || s.length() == 0)
            return false; // words must have at least one character
        if (illegalFirstChar.indexOf(s.charAt(0)) >= 0)
            return false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '(' ||
                    c == ')' || c <= 0x20)
                return false;
        }
        return true;
    }

}

