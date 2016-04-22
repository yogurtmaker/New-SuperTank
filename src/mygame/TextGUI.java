/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

  import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
 
 
public class TextGUI extends JDialog {
    private JLabel name;
    private JTextField inputName;
    private JButton save;
    private EndScreen endscreen;
 
    public TextGUI(EndScreen endScreen) {
        this.endscreen = endScreen;
        setSize(240,180);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 30));
 
        name = new JLabel("Name:");      
        inputName = new JTextField(14);     
        save = new JButton("Save");
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                endscreen.changeName(inputName.getText());
            }
        });
 
        getContentPane().setBackground(Color.WHITE);
        getContentPane().add(name);
        getContentPane().add(inputName);
        getContentPane().add(save);
 
        setAlwaysOnTop (true);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
 
   
}  

