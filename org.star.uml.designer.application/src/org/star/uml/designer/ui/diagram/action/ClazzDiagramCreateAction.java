package org.star.uml.designer.ui.diagram.action;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.MetamodelType;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.diagram.usecase.edit.commands.ActorCreateCommand;
import org.eclipse.uml2.diagram.usecase.edit.helpers.ActorEditHelper;
import org.eclipse.uml2.diagram.usecase.edit.helpers.UMLBaseEditHelper;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.internal.impl.ActorImpl;
import org.eclipse.uml2.uml.internal.impl.UMLFactoryImpl;
import org.osgi.framework.Bundle;
import org.star.uml.designer.application.Activator;
import org.star.uml.designer.base.constance.GlobalConstants;
import org.star.uml.designer.base.utils.CommonUtil;
import org.star.uml.designer.base.utils.EclipseUtile;
import org.star.uml.designer.service.dao.PmsDao;
import org.star.uml.designer.ui.diagram.action.interfaces.IStarUMLModelAction;
import org.star.uml.designer.ui.factory.StarUMLCommandFactory;
import org.star.uml.designer.ui.factory.StarUMLEditHelperFactory;
import org.star.uml.designer.ui.views.StarPMSModelView;
import org.star.uml.designer.ui.views.StarPMSModelViewUtil;
import org.star.uml.designer.ui.views.StarPMSModelView.TreeObject;
import org.star.uml.designer.ui.views.StarPMSModelView.TreeParent;

public class ClazzDiagramCreateAction extends Action implements IStarUMLModelAction{
	public static final String ACTION_ID = "CLASS_DIAGRAM";
	public static final String ACTION_URI = "org.eclipse.uml2.diagram.clazz.ClazzDiagram";
	public static final String ACTION_TITLE ="Create Class Diagram";
	public static final String ICON_PATH = "/icons/16.gif";
	public static final String DIAGRAM_EXTENSION = "umlclass";
	public IViewPart view = null;
	
	public ClazzDiagramCreateAction() {
		super();
		this.setText(ACTION_TITLE);
		this.setImageDescriptor(getImageDescriptor());
	}
	
	@Override
	public void run() {
		try{
		Map inputData = new HashMap();
		// "default" ?????????????????? ???????????? ?????? ?????? "default" ????????? ???????????? ????????? ????????? ?????? ??? ??? 
		// Index??? +1 ?????? ????????? ????????? ????????????.  
		String fileName = "default"+EclipseUtile.getDefaultUMLIdx();
		String fileFullName = "default"+EclipseUtile.getDefaultUMLIdx()+"."+DIAGRAM_EXTENSION;
		URI diagramURI = URI.createDeviceURI(GlobalConstants.PATH_PREFIX+"/Root/"+fileFullName);
		URI modelURI = URI.createDeviceURI(GlobalConstants.PATH_PREFIX+"/Root/"+GlobalConstants.DEFAULT_MODEL_FILE);
		
		EclipseUtile.createDiagram("Root",diagramURI,modelURI,ACTION_ID);
		
		// ?????? Tree??? ?????????????????? ????????????.
		IViewPart view_part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.findView(GlobalConstants.PluinID.STAR_PMS_MODEL_VIEW);
		StarPMSModelView modelView = (StarPMSModelView)view_part;
		// ????????? Tree??? ????????????.
		TreeSelection treeSelection = (TreeSelection)modelView.getTreeViewer().getSelection();
		Object[] o = treeSelection.toArray();
		TreeObject parent = (TreeObject)treeSelection.getFirstElement();
		String parentId = (String)parent.getData(GlobalConstants.StarMoedl.STAR_MODEL_ID);
		// ID??? ????????????.
		String objId = "_" + CommonUtil.randomKey() + "-GMK-em0Iv_Q";
		// ????????? Node??? ????????? ????????? ????????????.
		
		
		TreeObject treeObject = ((TreeParent)modelView.getTreeParent().getChildren()[2]).appendChield(modelView.getTreeParent().getChildren()[2].getParent(),fileName+"("+ACTION_ID+")"
								,GlobalConstants.StarMoedl.STAR_MODEL_FILE, fileName);
		
		inputData.put("name",fileName);
		inputData.put("userId",modelView.getTreeParent().getData("userId"));
		inputData.put("parentSeq",parent.getData(GlobalConstants.StarMoedl.STAR_MODEL_USECASE_SEQ));
		try{
			PmsDao pd = new PmsDao();
			
			inputData.put("seq", pd.clazzSeqMax());
			inputData.put("divId", pd.clazzDevelopIdMax());
			pd.clazzInsert(inputData);
		}catch(Exception e){
			e.printStackTrace();
		}
		treeObject.setData(GlobalConstants.StarMoedl.STAR_MODEL_USECASE_SEQ, inputData.get("seq"));
		treeObject.setData(GlobalConstants.StarMoedl.STAR_MODEL_USECASE_PARENT_SEQ, inputData.get("parentSeq"));
		treeObject.setData(GlobalConstants.StarMoedl.STAR_MODEL_EXTENSION, DIAGRAM_EXTENSION);
		treeObject.setData(GlobalConstants.StarMoedl.STAR_MODEL_CATEGORY, 
					       GlobalConstants.StarMoedl.STAR_CATEGORY_DIAGRAM);
		treeObject.setData(GlobalConstants.StarMoedl.STAR_MODEL_ID, objId);
		modelView.getTreeViewer().refresh();
		// ?????? ????????? ????????? ????????? ????????? ????????????.
		StarPMSModelViewUtil.addDiagramToModel("Root","_vq0uQM3LEd-GMK-em0Iv_Q",fileName,DIAGRAM_EXTENSION,
							  				   GlobalConstants.StarMoedl.STAR_CATEGORY_DIAGRAM,ACTION_ID,objId,GlobalConstants.UMLMoedl.UML_TYPE_PACKAGE_Element,inputData.get("parentSeq").toString(),inputData.get("seq").toString());
		}catch(Exception e){
			e.printStackTrace();
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
