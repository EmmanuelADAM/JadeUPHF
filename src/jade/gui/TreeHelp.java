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

package jade.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

/*
This class provide the help for the GUI of the DF
@author Tiziana Trucco - CSELT S.p.A.
@version $Date: 2003-11-20 11:55:37 +0100 (gio, 20 nov 2003) $ $ Revision: 1.3 $

 */

public class TreeHelp extends JDialog {

    /**
     * @serial
     */
    private final boolean playWithLineStyle = false;
    /**
     * @serial
     */
    private final String lineStyle = "Angled";

    //Optionally play with line styles.  Possible values are
    //"Angled", "Horizontal", and "None" (the default).
    /**
     * @serial
     */
    private JEditorPane htmlPane;
    /**
     * @serial
     */
    private URL helpURL;

    public TreeHelp(Frame owner, String title, String url) {

        super(owner, title);
        //setTitle(title);
        // added for reply to window closing
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                disposeAsync();
            }
        });
        setHTMLText(url);


    }

    public TreeHelp(Dialog owner, String title, String url) {

        super(owner, title);
        //setTitle(title);
        // added for reply to window closing
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                disposeAsync();
            }
        });


        setHTMLText(url);

    }


    private void setHTMLText(String url) {
        JPanel main = new JPanel();

        main.setLayout(new BorderLayout());

        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        htmlPane.setPreferredSize(new Dimension(500, 300));
        JScrollPane htmlView = new JScrollPane(htmlPane);


        try {
            htmlPane.setPage(getClass().getResource(url));
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL");
        }

        main.add(htmlView, BorderLayout.CENTER);
        setSize(500, 500);
        try {
            int x = getOwner().getX() + (getOwner().getWidth() - getWidth()) / 2;
            int y = getOwner().getY() + (getOwner().getHeight() - getHeight()) / 2;
            setLocation(Math.max(x, 0), Math.max(y, 0));
        } catch (Exception e) {
        }
        getContentPane().add(main, BorderLayout.CENTER);

    }


    public void disposeAsync() {

        class disposeIt implements Runnable {
            private final Window toDispose;

            public disposeIt(Window w) {
                toDispose = w;
            }

            public void run() {
                toDispose.dispose();
            }

        }

        EventQueue.invokeLater(new disposeIt(this));

    }


}
