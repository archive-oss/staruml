package org.star.uml.designer.ui.diagram.action;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramCommandStack;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.core.GMFEditingDomainFactory;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.MetamodelType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Location;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.runtime.notation.impl.DiagramImpl;
import org.eclipse.gmf.runtime.notation.impl.ShapeImpl;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.diagram.common.async.ApplySynchronizationCommand;
import org.eclipse.uml2.diagram.common.async.AsyncDiagramDeleteRequest;
import org.eclipse.uml2.diagram.common.async.SyncModelContext;
import org.eclipse.uml2.diagram.common.async.SyncModelNode;
import org.eclipse.uml2.diagram.usecase.edit.helpers.UMLBaseEditHelper;
import org.eclipse.uml2.diagram.usecase.edit.parts.ActorEditPart;
import org.eclipse.uml2.diagram.usecase.edit.parts.PackageEditPart;
import org.eclipse.uml2.diagram.usecase.navigator.UMLNavigatorItem;
import org.eclipse.uml2.diagram.usecase.part.UMLDiagramUpdater;
import org.eclipse.uml2.diagram.usecase.part.UMLVisualIDRegistry;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.internal.impl.ActorImpl;
import org.eclipse.uml2.uml.internal.impl.BehavioredClassifierImpl;
import org.eclipse.uml2.uml.internal.impl.PackageImpl;
import org.eclipse.uml2.uml.internal.impl.UMLFactoryImpl;
import org.osgi.framework.Bundle;
import org.star.uml.designer.application.Activator;
import org.star.uml.designer.base.constance.GlobalConstants;
import org.star.uml.designer.base.utils.CommonUtil;
import org.star.uml.designer.base.utils.EclipseUtile;
import org.star.uml.designer.command.MoveShapeCommand;
import org.star.uml.designer.ui.diagram.action.interfaces.IStarUMLModelAction;
import org.star.uml.designer.ui.factory.StarUMLCommandFactory;
import org.star.uml.designer.ui.factory.StarUMLEditHelperFactory;
import org.star.uml.designer.ui.views.StarPMSModelView;
import org.star.uml.designer.ui.views.StarPMSModelViewUtil;
import org.star.uml.designer.ui.views.StarPMSModelView.TreeObject;
import org.star.uml.designer.ui.views.StarPMSModelView.TreeParent;

public class DeleteFromDiagramAction extends Action implements IStarUMLModelAction{
	public static final String ACTION_ID = "DELETE FROM DIATRAM";
	public static final String ACTION_URI = "";
	public static final String ACTION_TITLE ="Delete from diagram";
	public static final String ACTION_TYPE ="";
	public static final String ICON_PATH = "/icons/logout.gif";
	
	private String selectedNodeName = "";
	private DiagramDocumentEditor editor = null;
	
	public DeleteFromDiagramAction() {
		super();
		this.setText(ACTION_TITLE);
		this.setImageDescriptor(getImageDescriptor());
	}
	
	@Override
	public void run() {
			// ?????? Tree??? Actor??? ????????????.
			IViewPart view_part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
													.findView(GlobalConstants.PluinID.STAR_PMS_MODEL_VIEW);
			StarPMSModelView modelView = (StarPMSModelView)view_part;
			// ????????? Tree??? ????????????.
			TreeSelection treeSelection = (TreeSelection)modelView.getTreeViewer().getSelection();
			TreeObject parent = (TreeObject)treeSelection.getFirstElement();
			selectedNodeName = (String)parent.getData(GlobalConstants.StarMoedl.STAR_MODEL_FILE);
			// ?????? ?????? ?????? Usecase Diagram Editor??? ?????? ??? ?????? ?????? ?????? ?????? ????????? ????????? ????????? ????????????.
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if(page.getActiveEditor() !=null && page.getActiveEditor() instanceof DiagramDocumentEditor){
				DiagramDocumentEditor editor = (DiagramDocumentEditor)page.getActiveEditor();
	        	// ????????? ????????? ???????????? ?????? ?????? ???????????? ????????????.
	        	IDiagramDocument document = editor.getDiagramDocument();
	        	Diagram diagram = document.getDiagram();
	        	TransactionalEditingDomain editingDomain = editor.getEditingDomain();
	        	IGraphicalEditPart ep = (IGraphicalEditPart)editor.getDiagramGraphicalViewer().getContents();
	    		// Editor??? ?????? ?????? ??? ????????? ????????? ?????? ?????? ?????? ??? ????????? ??? ????????????.
	        	for(int i=0; i<diagram.getPersistedChildren().size(); i++){
	        		ShapeImpl shapeImple = (ShapeImpl)diagram.getPersistedChildren().get(i);
	        		BehavioredClassifierImpl beImple = null;
	        		if(shapeImple.getElement() instanceof BehavioredClassifierImpl){
	        			beImple = (BehavioredClassifierImpl)shapeImple.getElement();
	        			if(selectedNodeName.equals(beImple.getName())){
	        				// ????????? ????????? ???????????? Delete Command??? ???????????? ????????????.
	        				Location location= (Location) shapeImple.getLayoutConstraint();
		        			Point point = new Point(location.getX(),location.getY());
		        			EditPart actorPart  = editor.getDiagramEditPart().getViewer().findObjectAt(point);
			        		CompoundCommand cmd = new CompoundCommand("Delete from diagram");
			        		AsyncDiagramDeleteRequest deleteReq = new AsyncDiagramDeleteRequest();
			        		deleteReq.setKind(AsyncDiagramDeleteRequest.Kind.NOTATION_ONLY);
			        		cmd.add(actorPart.getCommand(deleteReq));
			        		editor.getDiagramEditDomain().getDiagramCommandStack().execute(cmd);
		        		}
	        		}
	        	}
	        }
	}
	
	public URL getImageURL(){
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		return bundle.getEntry(ICON_PATH);
	}
	
	public ImageDescriptor getImageDescriptor(){
		return Activator.getImageDescriptor(ICON_PATH);
	}
}
