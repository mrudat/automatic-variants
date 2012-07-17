/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import java.io.IOException;
import lev.gui.LImagePane;
import skyproc.SPGlobal;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPSettingPanel;

/**
 *
 * @author Justin Swanson
 */
public class WelcomePage extends SPSettingPanel {

    LImagePane picture;

    public WelcomePage(SPMainMenuPanel parent_) {
	super("", parent_, AV.orange, AV.save);
    }

    @Override
    public boolean initialize() {
	if (super.initialize()) {

	    save.setVisible(false);
	    defaults.setVisible(false);
	    try {
		picture = new LImagePane(WelcomePage.class.getResource("AV welcome.png"));
		settingsPanel.add(picture);
	    } catch (IOException ex) {
		SPGlobal.logException(ex);
	    }

	    return true;
	}
	return false;
    }

    @Override
    public void specialOpen(SPMainMenuPanel parent) {
    }
}