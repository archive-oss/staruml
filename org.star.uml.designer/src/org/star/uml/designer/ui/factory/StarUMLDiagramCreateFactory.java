package org.star.uml.designer.ui.factory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.star.uml.designer.ui.diagram.action.ActivityDiagramCreateAction;
import org.star.uml.designer.ui.diagram.action.UsecaseDiagramCreateAction;


public class StarUMLDiagramCreateFactory {
	
	public static Resource getResource(String actionID,URI diagramURI,URI modelURI,IProgressMonitor monitor){
		Resource diagram = null;
		if(actionID.equals(UsecaseDiagramCreateAction.ACTION_ID)){
			diagram = org.eclipse.uml2.diagram.usecase.part.UMLDiagramEditorUtil.createDiagram(diagramURI, modelURI, null, "UTF-8", monitor);
		}else if(actionID.equals(ActivityDiagramCreateAction.ACTION_ID)){
			diagram = org.eclipse.uml2.diagram.usecase.part.UMLDiagramEditorUtil.createDiagram(diagramURI, modelURI, null, "UTF-8", monitor);
		}
		return diagram;
	}
}
