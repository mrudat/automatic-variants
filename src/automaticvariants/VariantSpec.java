/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants;

import java.io.Serializable;
import skyproc.SPGlobal;

/**
 *
 * @author Justin Swanson
 */
public class VariantSpec implements Serializable  {

    int Probability_Divider = 1;

    void print() {
	if (SPGlobal.logging()) {
	    SPGlobal.log("VariantSpec", "  Loaded specs: ");
	    SPGlobal.log("VariantSpec", "    Prob Div: 1/" + Probability_Divider);
	}
    }
}
