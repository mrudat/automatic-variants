/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import skyproc.SPGlobal;

/**
 *
 * @author Justin Swanson
 */
public class VariantMeshSet extends PackageNode {

    static String depth = "* +   ";
    public SpecVariant spec;

    VariantMeshSet(File groupDir) {
	super(groupDir, Type.MESHSET);
	spec = new SpecVariant(groupDir);
    }

    public void load() throws FileNotFoundException, IOException {
	if (SPGlobal.logging()) {
	    SPGlobal.log(src.getName(), depth + "### Adding Variant Mesh Set: " + src);
	}
	for (File globalMeshDir : src.listFiles()) {
	    if (globalMeshDir.isDirectory()) {
		if (SPGlobal.logging()) {
		    SPGlobal.log(src.getName(), depth + "# *** Adding Variant Mesh Set: " + globalMeshDir);
		}
		File globalMesh = null;
		for (File f : globalMeshDir.listFiles()) {
		    if (AVFileVars.isSpec(f)) {
			try {
			    spec = AV.gson.fromJson(new FileReader(f), SpecVariant.class);
			    if (spec != null) {
				spec.src = f;
				if (SPGlobal.logging()) {
				    spec.printToLog(src.getName());
				}
			    }
			} catch (com.google.gson.JsonSyntaxException ex) {
			    SPGlobal.logException(ex);
			    JOptionPane.showMessageDialog(null, "Variant Set Mesh " + f.getPath() + " had a bad specifications file.  Skipped.");
			}
		    } else if (AVFileVars.isNIF(f)) {
			globalMesh = f;
		    }
		}
		if (globalMesh == null) {
		    SPGlobal.log(src.getName(), depth + "# * Skipped Set Mesh: " + globalMeshDir);
		} else {
		    PackageNode globalMeshDirN = new PackageNode(globalMeshDir, Type.MESHSET);
		    add(globalMeshDirN);
		    PackageNode globalMeshN = new PackageNode(globalMesh, Type.GENMESH);
		    globalMeshDirN.add(globalMeshN);
		}
	    }
	}
	if (SPGlobal.logging()) {
	    SPGlobal.log(src.getName(), depth + "####################################");
	}
    }

    @Override
    public ArrayList<Variant> getVariants() {
	return new ArrayList<>(0);
    }

    public void mergeInGlobals(ArrayList<PackageNode> globalFiles) {
	for (Variant v : getVariants()) {
	    v.mergeInGlobals(globalFiles);
	}
    }

    public VariantSet getSet() {
	return (VariantSet) getParent();
    }

    @Override
    public String printName(String spacer) {
	PackageNode p = (PackageNode) this.getParent();
	return p.printName(spacer) + spacer + src.getName();
    }
}
