package org.star.uml.designer.ui.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.image.ImageFileFormat;
import org.eclipse.gmf.runtime.diagram.ui.render.clipboard.DiagramGenerator;
import org.eclipse.gmf.runtime.diagram.ui.render.clipboard.DiagramImageGenerator;
import org.eclipse.gmf.runtime.diagram.ui.render.internal.DiagramUIRenderPlugin;
import org.eclipse.gmf.runtime.diagram.ui.render.internal.DiagramUIRenderStatusCodes;
import org.eclipse.gmf.runtime.diagram.ui.render.internal.l10n.DiagramUIRenderMessages;
import org.eclipse.gmf.runtime.diagram.ui.render.util.CopyToImageUtil;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.star.uml.designer.application.Activator;
import org.star.uml.designer.base.constance.GlobalConstants;
import org.star.uml.designer.base.utils.EclipseUtile;
import org.star.uml.designer.base.utils.XmlUtil;
import org.star.uml.designer.service.dao.PmsDao;
import org.star.uml.designer.ui.action.PMSLoginAction;
import org.star.uml.designer.ui.action.PMSLogoutAction;
import org.star.uml.designer.ui.action.RefactorRenameAction;
import org.star.uml.designer.ui.action.ViewReportAction;
import org.star.uml.designer.ui.diagram.action.ActorCreateAction;
import org.star.uml.designer.ui.diagram.action.ClassModelCreateAction;
import org.star.uml.designer.ui.diagram.action.ClazzDiagramCreateAction;
import org.star.uml.designer.ui.diagram.action.DeleteDiagramAction;
import org.star.uml.designer.ui.diagram.action.DeleteFromDiagramAction;
import org.star.uml.designer.ui.diagram.action.DeleteFromModelAction;
import org.star.uml.designer.ui.diagram.action.InterfaceModelCreateAction;
import org.star.uml.designer.ui.diagram.action.PackageModelCreateAction;
import org.star.uml.designer.ui.diagram.action.PackageModelInsertAction;
import org.star.uml.designer.ui.diagram.action.SequenceDiagramCreateAction;
import org.star.uml.designer.ui.diagram.action.UsecaseModelCreateAction;
import org.star.uml.designer.ui.diagram.action.UsecaseDiagramCreateAction;
import org.star.uml.designer.ui.model.initialization.DefaultModel;
import org.star.uml.designer.ui.model.initialization.DefaultUML;
import org.star.uml.designer.ui.newWiazrds.ClassSorceCodeGeneration;
import org.star.uml.designer.ui.newWiazrds.db.ConnectionCreateDialog;
import org.star.uml.designer.ui.views.StarPMSModelView.TreeObject;
import org.star.uml.designer.ui.views.StarPMSModelView.TreeParent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StarPMSModelViewUtil {
	
	public static void loadModel(String project, String userId){
		// ModelView??? ????????????.
		IViewPart view_part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.findView(GlobalConstants.PluinID.STAR_PMS_MODEL_VIEW);
		StarPMSModelView modelView = (StarPMSModelView)view_part;
		// ?????? PMS??? ????????? ?????? ??? ??????????????? ?????? ?????? ???????????? ?????? ?????? ???????????? ????????????.
		IProject rootProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
		Document modelDoc = null;
		// ?????? ?????? ????????? ????????????.
		String projectPath = "";
		String modelPath = "";
		if(rootProject.exists()){
			try {
				projectPath = rootProject.getLocation().toOSString();
				modelPath =  projectPath+File.separator+GlobalConstants.DEFAULT_VIEW_MODEL_FILE;
				String domStr = XmlUtil.getXmlFileToString(modelPath);
				modelDoc = XmlUtil.getStringToDocument(domStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			try {
				modelDoc = XmlUtil.getStringToDocument(DefaultModel.getXML());
				Document umlDoc = XmlUtil.getStringToDocument(DefaultUML.getXML());
				IProject newProjectHandle = EclipseUtile.createNewProject(project,modelDoc,umlDoc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// XML??? ????????? ??? Import ???????????? ???????????? ????????? ????????????.
		Node rootNode = modelDoc.getChildNodes().item(0);
		Node subPkgNode = rootNode.getChildNodes().item(9);
		setTreeFormXML(subPkgNode,modelView.getTreeParent());
		modelView.getTreeParent().setData(GlobalConstants.STAR_USER_ID, userId);
		modelView.getTreeViewer().refresh();
	}
	
	public static void setTreeFormXML(Node pkgeElement,TreeParent parent){
		// ModelView??? ????????????.
		IViewPart view_part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.findView(GlobalConstants.PluinID.STAR_PMS_MODEL_VIEW);
		StarPMSModelView modelView = (StarPMSModelView)view_part;
		for(int i=0; i<pkgeElement.getChildNodes().getLength(); i++){
			Node subPkg = pkgeElement.getChildNodes().item(i);
			if(subPkg.getNodeName().equals("packagedElement")){
				NamedNodeMap attMap = subPkg.getAttributes();
				String attrName = attMap.getNamedItem(GlobalConstants.StarMoedl.STAR_MODEL_NAME).getNodeValue();
				String category = attMap.getNamedItem(GlobalConstants.StarMoedl.STAR_MODEL_CATEGORY).getNodeValue();
				TreeParent project = modelView.createTreeParent(attrName);
				TreeObject projectObject = modelView.createTreeObject(attrName);
				for(int y=0; y< attMap.getLength() ;y++){
					String key = attMap.item(y).getNodeName();
					String value = attMap.item(y).getNodeValue();
					project.setData(key, value);
					projectObject.setData(key, value);
				}
				// ????????? ???????????? ????????? ?????? TreeObject??? ????????????.
				if(category.equals(GlobalConstants.StarMoedl.STAR_CATEGORY_DIAGRAM) || 
				   category.equals(GlobalConstants.StarMoedl.STAR_CATEGORY_DIAGRAM_MODEL)){
					parent.addChild(projectObject);
				}else{
					parent.addChild(project);
				}
				if(subPkg.getChildNodes().getLength()>= 1){
					setTreeFormXML(subPkg,project);
				}
			}
		}
	}
	
	public static void addDiagramToModel(String project,String parentId, String name, 
										 String extension,String category,String diagramName,String objId,String type, String parentSeq, String seq){
		try{
			// ?????? ????????? ?????? ??????????????? ????????????.
			IProject rootProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
			Document modelDoc = null;
			// ????????? Document??? ????????????.
			String projectPath = rootProject.getLocation().toOSString();
			String modelPath = projectPath+File.separator+GlobalConstants.DEFAULT_VIEW_MODEL_FILE;
			String domStr = XmlUtil.getXmlFileToString(modelPath);
			modelDoc = XmlUtil.getStringToDocument(domStr);
			// Document ?????? Element ???  TagName??? "packagedElement"??? ???????????? ID??? ????????? ????????? ??? Node??? ????????????.
			NodeList n = modelDoc.getDocumentElement().getElementsByTagName("packagedElement");
			for(int i = 0; i < n.getLength(); i++){
				Node node = n.item(i);
				NamedNodeMap attrMap = node.getAttributes();
				String id = attrMap.getNamedItem(GlobalConstants.StarMoedl.STAR_MODEL_ID).getNodeValue();
				if(id.equals(parentId)){
					Element newNode = modelDoc.createElement("packagedElement");
					newNode.setAttribute(GlobalConstants.StarMoedl.STAR_MODEL_ID, objId);
					newNode.setAttribute(GlobalConstants.StarMoedl.STAR_MODEL_CATEGORY, category);
					newNode.setAttribute(GlobalConstants.StarMoedl.STAR_MODEL_TYPE, type);
					newNode.setAttribute(GlobalConstants.StarMoedl.STAR_MODEL_NAME, name+"("+diagramName+")");
					newNode.setAttribute(GlobalConstants.StarMoedl.STAR_MODEL_FILE, name);
					newNode.setAttribute(GlobalConstants.StarMoedl.STAR_MODEL_EXTENSION, extension);
					newNode.setAttribute(GlobalConstants.StarMoedl.STAR_MODEL_USECASE_SEQ, parentSeq);
					newNode.setAttribute(GlobalConstants.StarMoedl.STAR_MODEL_USECASE_SEQ, seq);
					node.appendChild(newNode);
					XmlUtil.writeXmlFile(modelDoc, modelPath);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void addModelToUML(String project,String objId, String type, String name){
		// UML ????????? ?????? ??????????????? ????????????.
		IProject rootProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
		Document modelDoc = null;
		try{
			// ????????? Document??? ????????????.
			String projectPath = rootProject.getLocation().toOSString();
			String modelPath = projectPath+File.separator+GlobalConstants.DEFAULT_MODEL_FILE;
			String domStr = XmlUtil.getXmlFileToString(modelPath);
			modelDoc = XmlUtil.getStringToDocument(domStr);
			NodeList n = modelDoc.getDocumentElement().getElementsByTagName("packagedElement");
			// Document ?????? Element ???  TagName??? "package"??? ???????????? ID??? ????????? ????????? ??? Node??? ????????????.
			Node rootEl = modelDoc.getDocumentElement();
			Element newNode = modelDoc.createElement(GlobalConstants.UMLMoedl.UML_TYPE_PACKAGE_Element);
			newNode.setAttribute(GlobalConstants.StarMoedl.STAR_MODEL_ID, objId);
			newNode.setAttribute(GlobalConstants.StarMoedl.STAR_MODEL_TYPE, type);
			newNode.setAttribute(GlobalConstants.StarMoedl.STAR_MODEL_NAME, name);
			rootEl.appendChild(newNode);
			XmlUtil.writeXmlFile(modelDoc, modelPath);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * ?????? ?????? ??? ?????? ????????? ???????????????. GlobalConstants.NODE_NAMES??? ???????????? ???????????? ??????????????? ???????????????.
	 * @param list
	 * @return
	 */
	public static String genNodeName(TreeParent parent){
		TreeObject[] treeObj = parent.getChildren();
		ArrayList<String> namelist = new ArrayList();
		for(int i=0; i<treeObj.length; i++){ // ?????? ???????????? ??? ????????????.
			namelist.add((String)treeObj[i].getData(GlobalConstants.StarMoedl.STAR_MODEL_FILE));
		}
		String nodeName = "";
		int nameIdx = 0;
		boolean loofFlag = true;
		while(loofFlag){ // ?????? ????????? ?????? ????????? ????????????, ????????? ???????????? ??????????????? ????????????.
			for(int i=0; i<GlobalConstants.NODE_NAMES.length; i++){
				if(nameIdx == 0 && !namelist.contains(GlobalConstants.NODE_NAMES[i])){
					nodeName = GlobalConstants.NODE_NAMES[i];
					loofFlag = false;
					break;
				}else if(nameIdx != 0 && !namelist.contains(GlobalConstants.NODE_NAMES[i]+nameIdx)){
					nodeName = GlobalConstants.NODE_NAMES[i]+nameIdx;
					loofFlag = false;
					break;
				}
			}
			nameIdx++;
		}
		return nodeName;
	}
	
	/**
	 *  ?????? ????????? ???????????? ????????? ???????????????.
	 * @param menuMgr
	 */
	public static void initContextMenu(MenuManager menuMgr){
		PMSLoginAction login = new PMSLoginAction();// ?????????
		PMSLogoutAction logout = new PMSLogoutAction();// ?????? ??????

		DeleteFromDiagramAction delete = new DeleteFromDiagramAction(); // ????????????????????? ?????? ??????
		DeleteDiagramAction deleteDiagram = new DeleteDiagramAction();  // ??????????????? ?????? ??????
		DeleteFromModelAction deletFromModel = new DeleteFromModelAction(); // ?????????????????? ?????? ??????
		
		MenuManager diagramGroup = new MenuManager("Add Diagram"); // ??????????????? ?????? ??????
		UsecaseDiagramCreateAction usecaseDiagram = new UsecaseDiagramCreateAction(); // ??????????????? ??????????????? ??????
		SequenceDiagramCreateAction sequenceDiagram = new SequenceDiagramCreateAction(); // ????????? ??????????????? ??????
		ClazzDiagramCreateAction clazzDiagram = new ClazzDiagramCreateAction(); // ????????? ??????????????? ??????
		
		MenuManager modelGroup = new MenuManager("Add Model"); // ?????? ??????
		ActorCreateAction actor = new ActorCreateAction(); // Actor ??????
		UsecaseModelCreateAction usecase = new UsecaseModelCreateAction(); // Usecase ??????
		PackageModelCreateAction packageModel = new PackageModelCreateAction(); // Package ??????
		ClassModelCreateAction classModel = new ClassModelCreateAction(); // Class ??????
		InterfaceModelCreateAction interfaceModel = new InterfaceModelCreateAction(); // interface ??????
		
		MenuManager viewGroup = new MenuManager("View"); // ?????????
		ViewReportAction viewReportAction = new ViewReportAction(); // ??????????????? ????????? ??????
		
		MenuManager refactorGroup = new MenuManager("Refactor"); // ???????????? ??????
		RefactorRenameAction reName = new RefactorRenameAction(); // ?????? ??????
		
		// ????????? ?????? ??????
		menuMgr.add(login);
		menuMgr.add(logout);
		menuMgr.add(new Separator());
//		menuMgr.add(delete);
		
		// ?????? ?????? ??????
		menuMgr.add(deletFromModel);
		menuMgr.add(deleteDiagram);
		menuMgr.add(new Separator());
		
		// ??????????????? ?????? ??????
		menuMgr.add(diagramGroup);
		diagramGroup.add(usecaseDiagram);
		diagramGroup.add(sequenceDiagram);
		diagramGroup.add(clazzDiagram);
		menuMgr.add(new Separator());
		
		// ?????? ?????? ??????
		menuMgr.add(modelGroup);
		modelGroup.add(packageModel);
		modelGroup.add(new Separator());
//		modelGroup.add(classModel);
//		modelGroup.add(interfaceModel);
		modelGroup.add(usecase);
		modelGroup.add(actor);
		menuMgr.add(new Separator());
		
		// ??? ?????? ??????
		menuMgr.add(viewGroup);
		viewGroup.add(viewReportAction);
		menuMgr.add(new Separator());
		
		// ???????????? ?????? ??????
		menuMgr.add(refactorGroup);
		refactorGroup.add(reName);
		
	}

}
