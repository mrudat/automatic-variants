/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.AVFileVars;
import automaticvariants.AVPackage;
import automaticvariants.PackageComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import lev.Ln;
import lev.gui.LButton;
import lev.gui.LImagePane;
import skyproc.SPGlobal;

/**
 *
 * @author Justin Swanson
 */
public class SettingsPackagesManager extends DefaultsPanel {

    public static PackageTree tree;
//    static DDSreader reader;
    LImagePane display;
    LButton enableButton;
    LButton disableButton;
    LButton otherSettings;
    JPopupMenu optionsMenu;
    JMenuItem enable;
    JMenuItem compress;

    public SettingsPackagesManager(EncompassingPanel parent_) {
	super("Texture Variants", AV.save, parent_);
    }

    @Override
    public boolean initialize() {
	if (super.initialize()) {

	    tree = new PackageTree(AVGUI.middleDimensions.width - 30,
		    AVGUI.middleDimensions.height - 165, parent.helpPanel);
	    tree.setLocation(AVGUI.middleDimensions.width / 2 - tree.getWidth() / 2, last.y + 10);
	    tree.setMargin(10, 5);
	    tree.removeBorder();
	    Add(tree);


	    defaults.setVisible(false);
	    enableButton = new LButton("Enable", defaults.getSize(), defaults.getLocation());
	    enableButton.setLocation(enableButton.getX(), tree.getY() + tree.getHeight() + 15);
	    enableButton.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    enableSelection(true);
		}
	    });
	    Add(enableButton);


	    save.setVisible(false);
	    disableButton = new LButton("Disable", save.getSize(), save.getLocation());
	    disableButton.setLocation(disableButton.getX(), tree.getY() + tree.getHeight() + 15);
	    disableButton.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    enableSelection(false);
		}
	    });
	    Add(disableButton);


	    otherSettings = new LButton("Other Settings");
	    otherSettings.centerIn(settingsPanel, defaults.getY());
	    Add(otherSettings);


	    optionsMenu = new JPopupMenu();
	    enable = new JMenuItem("Enable");
	    enable.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    enableSelection(enable.getText().equals("Enable"));
		}
	    });
	    optionsMenu.add(enable);
	    compress = new JMenuItem("Compress");
	    compress.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    compress();
		}
	    });
	    optionsMenu.add(compress);


	    //Add popup listener
	    MouseAdapter ma = new MouseAdapter() {

		private void myPopupEvent(MouseEvent e) {
		    int x = e.getX();
		    int y = e.getY();
		    JTree tree = (JTree) e.getSource();
		    TreePath path = tree.getPathForLocation(x, y);
		    if (path == null) {
			return;
		    }

		    tree.setSelectionPath(path);
		    PackageComponent sel = (PackageComponent) path.getLastPathComponent();

		    compress.setVisible(sel.type == PackageComponent.Type.PACKAGE
			    || sel.type == PackageComponent.Type.VARSET
			    || sel.type == PackageComponent.Type.ROOT);

		    if (sel.disabled) {
			enable.setText("Enable");
		    } else {
			enable.setText("Disable");
		    }

		    optionsMenu.show(tree, x + 10, y);
		}

		@Override
		public void mousePressed(MouseEvent e) {
		    if (e.isPopupTrigger()) {
			myPopupEvent(e);
		    }
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		    if (e.isPopupTrigger()) {
			myPopupEvent(e);
		    }
		}
	    };
	    tree.addMouseListener(ma);

	    //	    reader = new DDSreader();
	    //	    display = new LImagePane();
	    //	    display.setMaxSize(AVGUI.rightDimensions.width - 50, 0);
	    //	    display.setVisible(true);

	    //	    parent.helpPanel.addToBottomArea(display);
	    //	    display.setVisible(true);

	    try {
		loadPackageList();
	    } catch (FileNotFoundException ex) {
		SPGlobal.logException(ex);
	    } catch (IOException ex) {
		SPGlobal.logException(ex);
	    }

	    return true;
	}
	return false;
    }

    @Override
    public void specialOpen(EncompassingPanel parent) {
//	parent.helpPanel.addToBottomArea(display);
	parent.helpPanel.setBottomAreaHeight(AVGUI.rightDimensions.width - 50);

	otherSettings.addActionListener(((SettingsMainMenu)parent).packagesOtherPanel.getOpenHandler(parent));
    }

    public void enableSelection(boolean enable) {
	TreePath[] paths = tree.getSelectionPaths();
	for (TreePath p : paths) {
	    ((PackageComponent) p.getLastPathComponent()).enable(enable);
	}

	// adjust folders to enable/disable based on their contents
	for (TreePath p : paths) {
	    for (int i = p.getPathCount() - 1; i >= 0; i--) {
		PackageComponent node = (PackageComponent) p.getPathComponent(i);
		if (node.src.isDirectory()) {
		    if (node.disabled) {
			for (PackageComponent child : node.getAll()) {
			    if (!child.disabled) {
				node.disabled = false;
				break;
			    }
			}
		    } else {
			boolean allDisabled = true;
			for (PackageComponent child : node.getAll()) {
			    if (!child.disabled) {
				allDisabled = false;
				break;
			    }
			}
			if (allDisabled) {
			    node.disabled = true;
			}
		    }
		}
	    }
	}
	tree.repaint();
    }

    public void compress() {
	try {
	    PackageComponent p = (PackageComponent) tree.getSelectionPaths()[0].getLastPathComponent();
	    long before = p.fileSize();
	    if (SPGlobal.logging()) {
		SPGlobal.log(p.src.getName(), "Current file size: " + before + "->" + Ln.toMB(before) + "MB");
	    }

	    p.consolidateCommonFiles();
	    loadPackageList();
	    PackageComponent.rerouteFiles(p.getDuplicateFiles());
	    loadPackageList();

	    long after = p.fileSize();
	    if (SPGlobal.logging()) {
		SPGlobal.log(p.src.getName(), "After file size: " + after + "->" + Ln.toMB(after) + "MB");
		SPGlobal.log(p.src.getName(), "Reduced size by: " + (before - after) + "->" + Ln.toMB(before - after) + "MB");
	    }
	    JOptionPane.showMessageDialog(null, "<html>"
		    + "Compressed: " + p.src.getPath() + "<br>"
		    + "  Starting size: " + Ln.toMB(before) + " MB<br>"
		    + "    Ending size: " + Ln.toMB(after) + " MB<br>"
		    + "    Saved space: " + Ln.toMB(before - after) + " MB"
		    + "</html>");
	} catch (FileNotFoundException ex) {
	    SPGlobal.logException(ex);
	} catch (IOException ex) {
	    SPGlobal.logException(ex);
	}
    }

    public void editSpec() {
    }

    public void loadPackageList() throws FileNotFoundException, IOException {

	boolean logging = SPGlobal.logging();
	SPGlobal.logging(false);

	AVFileVars.importVariants();

	File inactivePackages = new File(AVFileVars.inactiveAVPackagesDir);
	if (inactivePackages.isDirectory()) {
	    for (File packageFolder : inactivePackages.listFiles()) {
		if (packageFolder.isDirectory()) {
		    AVPackage avPackage = new AVPackage(packageFolder);
		    AVFileVars.AVPackages.mergeIn(avPackage);
		}
	    }
	}

	SPGlobal.logging(logging);

	AVFileVars.AVPackages.sort();
	tree.setModel(new DefaultTreeModel(AVFileVars.AVPackages));
    }
}