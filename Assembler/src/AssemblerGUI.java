import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import java.awt.Color;
import javax.swing.JFrame;
public class AssemblerGUI extends JFrame {
    private final JTextArea assemblyArea;
    private final JTextArea machineArea;
    private final JButton copyButton;
    private final JButton SplitButton;

    Font font = new Font("Arial", Font.BOLD, 15);
    //text background col
    Color c0=new Color(68,70,84);
    //buttons col
    Color c1 = new Color(52,53,65);
    //frame col
    Color c2= new Color(52,53,65);
    //Text col
    Color cT =new Color(209,213,219);
    public AssemblerGUI() {
        super("Assembler");

        // Create text areas
        assemblyArea = new JTextArea(17, 40);
        assemblyArea.setBackground(c0);
        assemblyArea.setFont(font);
        assemblyArea.setForeground(cT);
        assemblyArea.setCaretColor(Color.CYAN);
        assemblyArea.setAutoscrolls(true);
        assemblyArea.setFocusable(true);

        machineArea = new JTextArea(16, 20);
        machineArea.setBackground(c0);
        machineArea.setForeground(cT);
        machineArea.setFont(font);
        machineArea.setEditable(false);
        machineArea.setAutoscrolls(true);


        // Create scroll panes for text areas
        JScrollPane assemblyScrollPane = new JScrollPane(assemblyArea);
        JScrollPane machineScrollPane = new JScrollPane(machineArea);
        // Create buttons
        JButton assembleButton = new JButton("Assemble");
        JButton clearButton = new JButton("Clear");
        SplitButton =new JButton("Split");
        copyButton = new JButton("Copy");
        copyButton.setEnabled(false);
        SplitButton.setEnabled(false);
        clearButton.setBackground(c1);
        clearButton.setForeground(Color.WHITE);
        copyButton.setBackground(c1);
        copyButton.setForeground(Color.WHITE);
        assembleButton.setBackground(c1);
        assembleButton.setForeground(Color.WHITE);
        SplitButton.setBackground(c1);
        SplitButton.setForeground(Color.WHITE);
        // Add action listeners to buttons
        assembleButton.addActionListener(new AssembleButtonListener());
        clearButton.addActionListener(new ClearButtonListener());
        copyButton.addActionListener(new CopyButtonListener());
        SplitButton.addActionListener(new SplitButtonListener());


        // Create panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(assembleButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(SplitButton);
        buttonPanel.setBackground(c2);

        // Add components to content pane
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(assemblyScrollPane, BorderLayout.WEST);
        contentPane.add(machineScrollPane, BorderLayout.EAST);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.setBackground(c2);

        // Set window properties
        setSize(820, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setBackground(c0);
        //setResizable(false);
        setAlwaysOnTop(true);
        Dimension D =new Dimension(820,600);
        setMinimumSize(D);
    }

    //Don't touch this :)
    boolean is_hex =false;
    private class AssembleButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Assemble the code
            System.out.println();
            Assembler assembler =new Assembler();
            String[] CODE =assemblyArea.getText().split("\n");
            LinkedList<String> assemblyCode =new LinkedList<>(List.of(CODE)) ;
            assemblyCode.add(assemblyCode.size(),"END of the code ya Ghaly");
            ArrayList<String> machineCode = new ArrayList<>();
            int numberOfLabels=0;
            int Line= 0;
            int i =0;
            String l;
            while(!(l=assemblyCode.get(i++)).equals("END of the code ya Ghaly")){
                if(l.contains("#")||l.replaceAll("\\s","").equals("")) continue;
                Line++;
                if(l.contains(":")) {
                    numberOfLabels++;
                    assembler.SETLabel(l.replaceAll("[\\s :]", ""), Line-numberOfLabels);
                }
            }
            Line =0;
            i=0;
            while(!(l=assemblyCode.get(i++)).equals("END of the code ya Ghaly")){
                if(l.contains(":")||l.contains("#")||l.replaceAll("\\s","").equals("")) continue;
                String OUTPUT =assembler.assemble(l,Line);
                machineCode.add(OUTPUT);
                Line++;
            }
            // Display the machine code
            machineArea.setText(String.join("\n", machineCode));
            // Enable copy button
            copyButton.setEnabled(true);
            SplitButton.setEnabled(true);
            is_hex=true;

        }
    }

    private class ClearButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Clear the text areas
            assemblyArea.setText("");
            machineArea.setText("");

            // Disable copy button
            copyButton.setEnabled(false);
        }
    }

    private class CopyButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Copy the machine code to clipboard
            System.out.println(" Copied ".toUpperCase());
            StringSelection selection = new StringSelection(machineArea.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            machineArea.setText("THANKS\nFOR\nUSING\nOUR\nASSEMBLER :)");
            is_hex=false;
        }
    }

    private class SplitButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(!is_hex) return;
            // Split the instructions to 8 bits.
            String[] CODE =machineArea.getText().split("\n");
            ArrayList<String> machineCode = new ArrayList<>();
            for(int i = 0 ; i<CODE.length;i++){
                machineCode.add(CODE[i].substring(0,2));
                machineCode.add(CODE[i].substring(2,4));
            }
            System.out.println(" Splited  ".toUpperCase());
            machineArea.setText(String.join("\n", machineCode));
            SplitButton.setEnabled(false);
        }
    }
    public static void main(String[] args) {
        System.out.println();
        System.out.println("# # # # # # # # # # # # # # #       Welcome to Our secret Helper     # # # # # # # # # # # # # #  # #".toUpperCase());
        System.out.println("Here you can see binary source of your code and if you have any error you may find good clarification");
        System.out.println();
        SwingUtilities.invokeLater(AssemblerGUI::new);

    }
}
