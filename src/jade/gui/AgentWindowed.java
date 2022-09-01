package jade.gui;

import javax.swing.*;

import static java.lang.System.out;


/**
 * GuiAgent linked to a simple window to display text
 *
 * @author revised by Emmanuel ADAM
 */
public class AgentWindowed extends GuiAgent {


    protected SimpleWindow4Agent window;

    /**
     * GUI
     */

    public AgentWindowed() {
    }


    /**
     * print a msg n the associated window
     */
    protected void println(String msg) {
        SwingUtilities.invokeLater(() -> window.println(msg));
    }
    /**print a text on the console*/

    /**print a formatted text with values on the console*/
    protected void printf(String format, Object[] tabO) {SwingUtilities.invokeLater(() ->window.printf(format, tabO));}

    /**
     * fonction a remplir pour repondre aux evenements de la fenetre
     * (par defaut, tue l'agent quand la fenetre est fermee)
     */
    @Override
    protected void onGuiEvent(GuiEvent evt) {
        if (evt.getType() == SimpleWindow4Agent.QUIT_EVENT) {
            //fermeture de la fenetre, on tue l'agent
            doDelete();
        }
    }

    /**
     * @return the window
     */
    public SimpleWindow4Agent getWindow() {
        return window;
    }


    /**
     * @param window the window to set
     */
    public void setWindow(SimpleWindow4Agent window) {
        this.window = window;
    }


}
