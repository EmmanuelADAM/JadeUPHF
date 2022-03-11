package jade.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/** a simple window with a text area to display informations
 * and a button
 * the window find automaticcaly its place in the screen
 * @author emmanueladam */
public class SimpleWindow4Agent extends JFrame  implements ActionListener {
    public final static int OK_EVENT = 1;
    public final static int QUIT_EVENT = -1;
    static int nb = 0;
    int no = 0;
    /**
     * Text area
     */
    JTextArea jTextArea;
    /**
     * Text area
     */
    JButton jbutton;
    /**
     * monAgent linked to this frame
     */
    AgentWindowed myAgent;
    private boolean buttonActivated;


    /** a simple window with a text area to display informations
     * and a button
     * the window find automaticcaly its place in the screen
     * @author emmanueladam */
    public SimpleWindow4Agent() {
        no = nb++;
        int widthJFrame = 450;
        int heightJFrame = 200;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int nbWindowsByRow = (int)Math.floor(screen.getWidth()/widthJFrame);
        int xx =  ((no%nbWindowsByRow) * (widthJFrame+5));
        int yy =  (((no/nbWindowsByRow)*(heightJFrame+10)))%((int)screen.getHeight());
        setBounds(xx, yy, widthJFrame, heightJFrame);
        buildGui();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                GuiEvent ev = new GuiEvent(this, QUIT_EVENT);
                myAgent.postGuiEvent(ev);
            }
        });
        setVisible(true);
    }

    public SimpleWindow4Agent(AgentWindowed _a) {
        this();
        myAgent = _a;
    }


    public SimpleWindow4Agent(String _titre) {
        this();
        setTitle(_titre);
    }

    public SimpleWindow4Agent(String _titre, AgentWindowed _a) {
        this(_titre);
        myAgent = _a;
    }


    /**
     * build the gui : a text area in the center of the window, with scroll bars
     */
    private void buildGui() {
        getContentPane().setLayout(new BorderLayout());
        jTextArea = new JTextArea();
        jTextArea.setRows(5);
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        getContentPane().add(BorderLayout.CENTER, jScrollPane);
        jbutton = new JButton("--");
        getContentPane().add(BorderLayout.SOUTH, jbutton);
        jbutton.setEnabled(false);
    }


    /**
     * add a string to the text area
     */
    public void println(String chaine) {
        String texte = jTextArea.getText();
        texte = texte + chaine + "\n";
        jTextArea.setText(texte);
        jTextArea.setCaretPosition(texte.length());
    }

    /**
     * SEND A MESSAGE TO THE AGENT
     */
    public void actionPerformed(ActionEvent evt) {
        GuiEvent ev = new GuiEvent(this, OK_EVENT);
        myAgent.postGuiEvent(ev);
    }

    public void setBackgroundTextColor(Color c)
    {
        jTextArea.setBackground(c);
    }

    public boolean isButtonActivated() {
        return buttonActivated;
    }

    public void setButtonActivated(boolean buttonActivated) {
        if(buttonActivated)
        {
            jbutton.setEnabled(true);
            jbutton.setText("-- go --");
            jbutton.addActionListener(this);
        }
        else
        {
            jbutton.setEnabled(false);
            jbutton.setText("--");
            jbutton.addActionListener(null);
        }
        this.buttonActivated = buttonActivated;
    }

}
