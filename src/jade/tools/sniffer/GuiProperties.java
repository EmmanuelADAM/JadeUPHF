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


package jade.tools.sniffer;

import javax.swing.*;

/**
 * Javadoc documentation for the file
 *
 * @author Francisco Regi, Andrea Soracchi - Universita` di Parma
 * <Br>
 * <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
 * @version $Date: 2003-03-11 11:55:44 +0100 (mar, 11 mar 2003) $ $Revision: 3784 $
 */

/**
 * This class loads the icons used in the toolbar and menus
 */

public class GuiProperties {

    public static final String ImagePath = "";
    protected static UIDefaults MyDefaults;
    protected static GuiProperties foo = new GuiProperties();

    static {
        Object[] icons = {
                "SnifferAction.ClearCanvasActionIcon", FlatIcon.broom(),
                "SnifferAction.DisplayLogFileActionIcon", FlatIcon.folderOpen(),
                "SnifferAction.WriteLogFileActionIcon", FlatIcon.save(),
                "SnifferAction.MessageFileActionIcon", FlatIcon.saveList(),
                "SnifferAction.DoSnifferActionIcon", FlatIcon.dot(SnifferTheme.AGENT_ACTIVE),
                "SnifferAction.DoNotSnifferActionIcon", FlatIcon.dot(SnifferTheme.AGENT_EXCLUDED),
                "SnifferAction.ShowOnlyActionIcon", FlatIcon.ring(SnifferTheme.AGENT_ACTIVE),
                "SnifferAction.ExitActionIcon", FlatIcon.exit(),
        };
        MyDefaults = new UIDefaults(icons);
    }

    public static final Icon getIcon(String key) {
        Icon i = MyDefaults.getIcon(key);
        if (i == null) {
            System.out.println("Mistake with Icon");
            System.exit(-1);
            return null;
        } else
            return MyDefaults.getIcon(key);
    }

}  // End of GuiProperties
