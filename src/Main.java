/** 
 * Copyright (c) Arjun Nair 2017
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.spell.SpellingParser;

public class Main
{
	private static RSyntaxTextArea editorPane;
	private static SpellingParser spellParser;
	private static JFrame mainScreen;
	private static String filename = "";
	public static void main(String[] args)
	{
		mainScreen = new JFrame("Yogurt Bowl");
		mainScreen.setIconImage(new ImageIcon("images/ParfA-logo.png").getImage());
		WindowAdapter adapter = new WindowAdapter()
		{
			public void windowClosing(WindowEvent arg0) 
			{
				System.exit(0);
			}
		};
		mainScreen.addWindowListener(adapter);
		Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(mainScreen.getGraphicsConfiguration());
		int taskbarSize = scnMax.bottom;
		Dimension dimMax = Toolkit.getDefaultToolkit().getScreenSize();
		mainScreen.setSize(dimMax.width/3, dimMax.height - taskbarSize);
		mainScreen.setLayout(new BorderLayout());
		editorPane = new RSyntaxTextArea("announce \"Hello world!\"");
		editorPane.setFont(new Font("Consolas", Font.PLAIN, 15));
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("ParfAHighlighter", "ParfAHighlighter");
		editorPane.setSyntaxEditingStyle("ParfAHighlighter");
	    editorPane.setCodeFoldingEnabled(true);
		try 
		{
			spellParser = SpellingParser.createEnglishSpellingParser(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "syntaxarea/english_dic.zip"), true);
		} catch (IOException ioe) {}
		if (spellParser != null) 
		{
			try 
			{
				File userDict= File.createTempFile("spellDemo", ".txt");
				spellParser.setUserDictionary(userDict);
			} 
			catch (Exception e) {}
			SwingUtilities.invokeLater(
					new Runnable() 
					{
						@Override
						public void run() 
						{
							editorPane.addParser(spellParser);
						}
					}
			);
		}
		JScrollPane editorScrollPane = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,4));
		JButton runButton = new JButton("Run");
		ActionListener runButtonListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Thread run = new Thread()
				{
					public void run()
					{
						Main.run();
					}
				};
				run.start();
			}
		};
		runButton.addActionListener(runButtonListener);
		JButton openFileButton = new JButton("Open");
		ActionListener openFileButtonListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				openFile();
			}
		};
		openFileButton.addActionListener(openFileButtonListener);
		JButton saveButton = new JButton("Save");
		ActionListener saveButtonListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				save();
			}
		};
		saveButton.addActionListener(saveButtonListener);
		JButton saveAsButton = new JButton("Save As");
		ActionListener saveAsButtonListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				saveAs();
			}
		};
		saveAsButton.addActionListener(saveAsButtonListener);
		buttonPanel.add(runButton);
		buttonPanel.add(openFileButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(saveAsButton);
		mainScreen.add(editorScrollPane, BorderLayout.CENTER);
		mainScreen.add(buttonPanel, BorderLayout.SOUTH);
		mainScreen.setLocation(dimMax.width/3, 0);
		mainScreen.setVisible(true);
	}
	
	public static void openFile()
	{
		JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("ParfA Code Files", "txt", "text"));
        fc.setCurrentDirectory(new File(System.getProperty("user.home") + "/Documents/ParfA/"));
        fc.showOpenDialog(mainScreen);
		try 
		{
	        filename = fc.getSelectedFile().getAbsolutePath();
			editorPane.setText(new String(Files.readAllBytes(Paths.get(new File(filename).getPath())), StandardCharsets.UTF_8));
		} 
		catch (IOException e) {}
		catch(NullPointerException e) {}
	}
	public static void run()
	{
		ParfA.refreshConsole();
		ParfA.run(editorPane.getText());
	}
	public static void save()
	{
		if(!filename.equals(""))
			try
			{
				File f = new File(filename);
				f.createNewFile();
				FileWriter fileWriter = new FileWriter(f);
				fileWriter.write(editorPane.getText());
				fileWriter.flush();
				fileWriter.close();
			}
			catch(IOException e) {}
		else
			saveAs();
	}
	public static void saveAs()
	{
		filename = System.getProperty("user.home") + "/Documents/ParfA/" + JOptionPane.showInputDialog("What is the name of your program? (exclude .txt)") + ".txt";
		save();
	}
}