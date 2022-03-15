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


package jade.tools.DummyAgent;

import jade.core.AID;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;
import jade.util.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * @author Giovanni Caire - CSELT S.p.A
 * @version $Date: 2008-10-09 14:04:02 +0200 (gio, 09 ott 2008) $ $Revision: 6051 $
 */

class MsgIndication {
    public static final int INCOMING = 0;
    public static final int OUTGOING = 1;
    private static final int TYPE_LEN = 20;
    //logging
    private static final Logger logger = Logger.getMyLogger(MsgIndication.class.getName());
    private static final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    public ACLMessage msg;
    public int direction;
    public Date date;

    MsgIndication() {
        msg = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
        direction = OUTGOING;
        date = new Date();
    }

    MsgIndication(ACLMessage m, int dir, Date d) {
        msg = (ACLMessage) m.clone();
        direction = dir;
        date = d;
    }

    static MsgIndication fromText(BufferedReader r) {
        MsgIndication mi = new MsgIndication();
        try {
            String line;

            // Date
            line = r.readLine();
            mi.date = df.parse(line);

            // Direction
            mi.direction = Integer.parseInt(r.readLine());

            // Message length
            int len = Integer.parseInt(r.readLine());

            // Message
            char[] cBuf = new char[len];
            r.read(cBuf, 0, len);

            StringACLCodec codec = new StringACLCodec(new StringReader(new String(cBuf)), null);
            mi.msg = codec.decode();

            // Read the last newline
            line = r.readLine();

        } catch (IOException e) {
            if (logger.isLoggable(Logger.WARNING))
                logger.log(Logger.WARNING, "IO Exception in MsgIndication.fromText()");
        } catch (java.text.ParseException e1) {
            if (logger.isLoggable(Logger.WARNING))
                logger.log(Logger.WARNING, "ParseException in MsgIndication.fromText()");
        } catch (ACLCodec.CodecException e2) {
            if (logger.isLoggable(Logger.WARNING))
                logger.log(Logger.WARNING, "ParseException in parsing the ACL message");
        } //Exception thrown by ACLMessage.fromText()

        return (mi);
    }

    String getIndication() {
        int perf = msg.getPerformative();
        StringBuilder tmpType = new StringBuilder(ACLMessage.getPerformative(perf));
        int blancCharCnt = TYPE_LEN - tmpType.length();
        while (blancCharCnt-- > 0)
            tmpType.append(" ");

        // Put the destination agent group in form of a string
        StringBuilder dest = new StringBuilder();
        Iterator<AID> destAG = msg.getAllReceiver();

        while (destAG.hasNext())
            dest.append((destAG.next()).getName()).append(" ");

        String tmpDir = (direction == OUTGOING ? "sent to  " : "recv from");

        String tmpPeer = (direction == OUTGOING ? dest.toString() : msg.getSender().getName());
        return (df.format(date) + ":  " + tmpType + " " + tmpDir + "   " + tmpPeer);
    }

    ACLMessage getMessage() {
        return (msg);
    }

    void setMessage(ACLMessage m) {
        msg = (ACLMessage) m.clone();
    }

    public String toString() {
        return (df.format(date) + "\n" + direction + "\n" + msg.toString());
    }

    void toText(BufferedWriter w) {
        try {
            // Date
            w.write(df.format(date));
            w.newLine();
            // Direction
            w.write(String.valueOf(direction));
            w.newLine();
            // Message length
            String tmp = msg.toString();
            w.write(String.valueOf(tmp.length()));
            w.newLine();
            // Message
            w.write(tmp);
            w.newLine();

            w.flush();
        } catch (IOException e) {
            if (logger.isLoggable(Logger.WARNING))
                logger.log(Logger.WARNING, "IO Exception in MsgIndication.toText()");
        }
    }
}





