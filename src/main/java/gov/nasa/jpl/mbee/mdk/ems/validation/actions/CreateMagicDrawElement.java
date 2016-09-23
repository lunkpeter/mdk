/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.mdk.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.ems.ImportUtility;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.*;

public class CreateMagicDrawElement extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private JSONObject ob;
    private Map<String, JSONObject> elementsKeyed;
    private Collection<Annotation> annos;
    private boolean multiple = false;
    private boolean multipleSuccess = true;

    public CreateMagicDrawElement(JSONObject ob, Map<String, JSONObject> elementsKeyed) {
        super("CreateMagicDrawElement", "Create MagicDraw element", null, null);
        this.ob = ob;
        this.elementsKeyed = elementsKeyed;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {
        multiple = false;
        multipleSuccess = true;
        this.annos = annos;
        executeMany(annos, "Create Elements");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        execute("Create Element");
    }

    @Override
    protected boolean doAction(Annotation anno) {
        if (anno != null) {
            if (multiple) {
                return multipleSuccess;
            }
            multiple = true;
            List<JSONObject> tocreate = new ArrayList<JSONObject>();
            for (Annotation ann : annos) {
                String message = ann.getText();
                String[] mes = message.split("`");
                String eid = null;
                if (mes.length > 2) {
                    eid = mes[1];
                }
                if (eid != null) {
                    JSONObject newe = elementsKeyed.get(eid);
                    if (newe != null) {
                        tocreate.add(newe);
                    }
                }
            }
            ImportUtility.CreationOrder creationOrder = ImportUtility.getCreationOrder(tocreate);
            tocreate = creationOrder.getOrder();
            Set<JSONObject> fail = creationOrder.getFailed();
            if (!fail.isEmpty()) {
                Utils.guilog("[ERROR] Cannot create elements (owner(s) not found)");
                multipleSuccess = false;
                return false;
            }
            else {
                ImportUtility.setShouldOutputError(false);
                for (JSONObject newe : tocreate) {
                    try {
                        Element newElement = ImportUtility.createElement(newe, false);
                        if (newElement == null) {
                            Utils.guilog("[ERROR] Cannot create element " + newe.get("sysmlId") + " (owner not found)");
                            multipleSuccess = false;
                            return false;
                        }
                    } catch (ImportException ex) {

                    }
                }
                ImportUtility.setShouldOutputError(true);
                for (JSONObject newe : tocreate) {
                    try {
                        Element newElement = ImportUtility.createElement(newe, true);
                        if (newElement == null) {
                            Utils.guilog("[ERROR] Cannot create element " + newe.get("sysmlId") + " (references not found)");
                            multipleSuccess = false;
                            return false;
                        }
                    } catch (ImportException ex) {
                        Utils.guilog("[ERROR] Cannot create element " + newe.get("sysmlId") + " (references not found)");
                        multipleSuccess = false;
                        return false;
                    }
                }
            }
        }
        else {
            try {
                Element magicDrawElement = ImportUtility.createElement(ob, false);
                magicDrawElement = ImportUtility.createElement(ob, true);
                if (magicDrawElement == null) {
                    Utils.guilog("[ERROR] Cannot create element (references or owner not found)");
                    return false;
                }
            } catch (ImportException ex) {
                Utils.guilog("[ERROR] Cannot create element (references or owner not found)");
                return false;
            }
        }
        return true;
    }
}