package org.star.uml.designer.ui.diagram.action;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.document.IDiagramDocument;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.MetamodelType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.notation.Diagram;
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
import org.eclipse.uml2.diagram.usecase.edit.helpers.UMLBaseEditHelper;
import org.eclipse.uml2.diagram.usecase.navigator.UMLNavigatorItem;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.internal.impl.ActorImpl;
import org.eclipse.uml2.uml.internal.impl.PackageImpl;
import org.eclipse.uml2.uml.internal.impl.UMLFactoryImpl;
import org.osgi.framework.Bundle;
import org.star.uml.designer.application.Activator;
import org.star.uml.designer.base.constance.GlobalConstants;
import org.star.uml.designer.base.utils.CommonUtil;
import org.star.uml.designer.base.utils.EclipseUtile;
import org.star.uml.designer.ui.diagram.action.interfaces.IStarUMLModelAction;
import org.star.uml.designer.ui.factory.StarUMLCommandFactory;
import org.star.uml.designer.ui.factory.StarUMLEditHelperFactory;
import org.star.uml.designer.ui.views.StarPMSModelView;
import org.star.uml.designer.ui.views.StarPMSModelViewUtil;
import org.star.uml.designer.ui.views.StarPMSModelView.TreeObject;
import org.star.uml.designer.ui.views.StarPMSModelView.TreeParent;

public class UsecaseModelCreateAction extends Action implements IStarUMLModelAction{
	public static final String ACTION_ID = "USECASE";
	public static final String ACTION_URI = "org.eclipse.uml2.diagram.usecase.createUseCase_2003";
	public static final String ACTION_TITLE ="Create Usecase";
	public static final String ACTION_TYPE ="uml:UseCase";
	public static final String ICON_PATH = "/icons/diagram/UseCase.gif";
	public String nodeName = "";
	
	public TransactionalEditingDomain domain = null;
	public DiagramDocumentEditor editor = null;
	public View view = null;
	
	public UsecaseModelCreateAction() {
		super();
		this.setText(ACTION_TITLE);
		this.setImageDescriptor(getImageDescriptor());
	}
	
	@Override
	public void run() {
		// ?????? Tree??? Usecase??? ????????????.
		IViewPart view_part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.findView(GlobalConstants.PluinID.STAR_PMS_MODEL_VIEW);
		StarPMSModelView modelView = (StarPMSModelView)view_part;
		// ????????? Tree??? ????????????.
		TreeSelection treeSelection = (TreeSelection)modelView.getTreeViewer().getSelection();
		TreeParent parent = (TreeParent)treeSelection.getFirstElement();
		String parentId = (String)parent.getData(GlobalConstants.StarMoedl.STAR_MODEL_ID);
		// ID??? ????????????.
		String objId = "_" + CommonUtil.randomKey() + "-tfMWeTSJUPw";
		// ????????? ????????????.
		nodeName = StarPMSModelViewUtil.genNodeName(parent);
		// ????????? Node??? ????????? ????????? ????????????.
		TreeObject treeObject = parent.appendChield(parent,nodeName+"("+ACTION_ID+")",
					GlobalConstants.StarMoedl.STAR_MODEL_CATEGORY, 
					GlobalConstants.StarMoedl.STAR_CATEGORY_DIAGRAM_MODEL					);
		treeObject.setData(GlobalConstants.StarMoedl.STAR_MODEL_FILE, nodeName);
		treeObject.setData(GlobalConstants.StarMoedl.STAR_MODEL_EXTENSION, ACTION_TYPE);
		treeObject.setData(GlobalConstants.StarMoedl.STAR_MODEL_ID, objId);
		modelView.getTreeViewer().refresh();
		// Model.xml ????????? ????????? ????????????.
		StarPMSModelViewUtil.addDiagramToModel("Root",parentId,nodeName,ACTION_TYPE,
							  			GlobalConstants.StarMoedl.STAR_CATEGORY_DIAGRAM_MODEL,ACTION_ID,objId,ACTION_TYPE,"","");
		// Default.xml ????????? ????????? ????????????.
		StarPMSModelViewUtil.addModelToUML("Root",objId,ACTION_TYPE,nodeName);
		EclipseUtile.refreshProject("Root");
	}
	
//	public EObject createNode(){
//		UMLFactory factoryImple = UMLFactoryImpl.init();
//		final ActorImpl actor = (ActorImpl)factoryImple.createActor();
//		actor.setName(nodeName);
//		return actor;
//	}

	public URL getImageURL(){
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
		return bundle.getEntry(ICON_PATH);
	}
	
	public ImageDescriptor getImageDescriptor(){
		return Activator.getImageDescriptor(ICON_PATH);
	}

}
