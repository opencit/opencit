/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.ui.lanterna;

import com.googlecode.lanterna.TerminalFacade;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.setup.SetupException;
import com.intel.mtwilson.setup.SetupWizard;
import org.apache.commons.configuration.Configuration;
import com.googlecode.lanterna.gui.*;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalSize;
import com.intel.mtwilson.setup.Platform;
import java.nio.charset.Charset;

/**
 * References:
 * Using Terminal: http://code.google.com/p/lanterna/source/browse/UsingTerminal.wiki?repo=wiki
 * Using Screen: http://code.google.com/p/lanterna/source/browse/UsingScreen.wiki?repo=wiki
 * Using GUI: 
 *   Start: http://code.google.com/p/lanterna/source/browse/GUIGuideStartTheGUI.wiki?repo=wiki
 *   Windows: http://code.google.com/p/lanterna/source/browse/GUIGuideWindows.wiki?repo=wiki
 *   Components: http://code.google.com/p/lanterna/source/browse/GUIGuideComponents.wiki?repo=wiki
 *   Dialogs: http://code.google.com/p/lanterna/source/browse/GUIGuideDialogs.wiki?repo=wiki
 *   Misc: http://code.google.com/p/lanterna/source/browse/GUIGuideMisc.wiki?repo=wiki
 * 
 * @author jbuhacoff
 */
public class Main  {

//    @Override
    public void execute(String[] args) throws SetupException {
        
        // createTextTerminal requires the file stty.exe
        // createUnixTermainal requires the file /bin/sh
        Terminal terminal ;
        if( Platform.isWindows() ) {
            terminal = TerminalFacade.createSwingTerminal();
        }
        else if( Platform.isUnix() ) {
            terminal = TerminalFacade.createUnixTerminal(Charset.forName("UTF8")); // can also pass Charset.forName("UTF8"),  or System.in, System.out, Charset.forName("UTF8")
        }
        else {
            System.err.println("Unrecognized platform: "+System.getProperty("os.name"));
            return;
        }
//        terminal.enterPrivateMode();
//        terminal.clearScreen();  // not needed in the beginning as it's done by default; but we could do it anytime later
//        terminal.flush(); // ensure our actions are written to the terminal
        
//        TerminalSize screenSize = terminal.getTerminalSize();
//        terminal.moveCursor(screenSize.getColumns() - 1, screenSize.getRows() - 1);  //Place the cursor in the bottom right corner
        
        Screen screen = new Screen(terminal);
        
        screen.startScreen();
        /*
        screen.putString(10, 5, "Hello Lanterna!", Terminal.Color.RED, Terminal.Color.GREEN);
        screen.refresh();
        
        boolean keepRunning = true;
        while(keepRunning) {
            Key key = screen.readInput();
            if( key != null ) {
                if( key.getCharacter() == 'x' ) { keepRunning = false; }
                System.err.println("hit character "+key.getCharacter());
            }
        }
        
        * */
        GUIScreen gui = new GUIScreen(screen);
        if( gui == null ) {
            System.err.println("Cannot initialize terminal");
            return;
        }
        
        MyWindow wnd = new MyWindow();
        gui.showWindow(wnd);
        

        screen.stopScreen();
//        terminal.exitPrivateMode();
    }
    
    /**
     *
     */
    public class MyWindow extends Window {
        public MyWindow() {
            super("my window");
            Panel horisontalPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);
            Panel leftPanel = new Panel(new Border.Standard(), Panel.Orientation.VERTICAL);
            Panel middlePanel = new Panel(new Border.Bevel(false), Panel.Orientation.VERTICAL);
            Panel rightPanel = new Panel(new Border.Invisible(), Panel.Orientation.VERTICAL);
            Panel farRightPanel = new Panel(new Border.Bevel(true), Panel.Orientation.VERTICAL);

            horisontalPanel.addComponent(leftPanel);
            horisontalPanel.addComponent(middlePanel);
            horisontalPanel.addComponent(rightPanel);
            horisontalPanel.addComponent(farRightPanel);

            addComponent(horisontalPanel);
        }
    }
}
