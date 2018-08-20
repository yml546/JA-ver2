package graph.cfg;

import graph.basic.AbstractGraph;
import graph.basic.GraphEdge;
import graph.basic.GraphNode;
import graph.basic.GraphUtil;
import graph.cfg.analyzer.ReachNameAnalyzer;
import graph.cfg.analyzer.ReachNameDefinition;
import graph.cfg.analyzer.ReachNameRecorder;
import graph.cfg.creator.ExecutionPointFactory;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameReference.NameReference;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import sourceCodeAST.CompilationUnitRecorder;

/**
 * The class of the control flow graph. 
 * @author Zhou Xiaocong
 * @since 2012/12/26
 * @version 1.01
 * @update 2013/06/12 Zhou Xiaocong
 * 		Add the implementations of the methods isVirtual(), isNormalEnd(), isAbnormalEnd(), isStart(), isPredicate() declared in the interface CFGNode
 * 		Add the method simplyWriteToDotFile() to override the method in AbstractGraph for generating a better graph visualization for CFG
 * @update 2013/09/13 Zhou Xiaocong
 * 		Modify simplyWriteToDotFile() to make it not write '\r' or '\n' in the label of a node!
 * 
 */
public class ControlFlowGraph extends AbstractGraph implements CFGNode {
	private String label = null;
	private String description = null;
	
	CompilationUnitRecorder unitRecorder = null;// The compilation unit message of the CFG
	ExecutionPointFactory factory = null;		// The factor for creating execution point
	
	private MethodDeclaration method = null;	// The AST node of the method corresponding to the CFG
	private String methodName = null;			// The name of the method corresponding to the CFG
	private String className = null;			// The class name of the class contains the method
	
	private CFGNode startNode = null;			// The start node of the entire CFG
	private CFGNode endNode = null;				// The normal end node of the entire CFG
	private CFGNode abnormalEndNode = null;		// The abnormal end node (i.e. throw exceptions) of the entire CFG

	
	public ControlFlowGraph(String id, String label, String description) {
		super(id);
		this.label = label;
		this.description = description;
	}

	/**
	 * Set the information about the source file to be create CFG
	 * @param fullFileName : the file name of the source file
	 * @param root : the AST root of the source file
	 */
	public void setCompilationUnitRecorder(String fullFileName, CompilationUnit root) {
		unitRecorder = new CompilationUnitRecorder(fullFileName, root);
	}
	
	public void setCompilationUnitRecorder(CompilationUnitRecorder recorder) {
		this.unitRecorder = recorder;
	}
	
	public CompilationUnitRecorder getCompilationUnitRecorder() {
		return unitRecorder;
	}
	
	public CompilationUnit getCompilationUnitRoot() {
		return unitRecorder.root;
	}
	
	public String getFileUnitName() {
		return unitRecorder.unitName;
	}

	public void setExecutionPointFactory(ExecutionPointFactory factory) {
		this.factory = factory;
	}
	
	public ExecutionPointFactory getExecutionPointFactory() {
		return factory;
	}
	
	/**
	 * Set the information about the method to be create CFG
	 * @param className : the class name of the method
	 * @param methodName : the name of the method
	 * @param method : the AST node of the method declaration
	 */
	public void setMethod(String className, String methodName, MethodDeclaration method) {
		this.className = className;
		this.methodName = methodName;
		this.method = method;
	}

	public void setAndAddStartNode(CFGNode startNode) {
		this.startNode = startNode;
		addNode(startNode);
	}

	public void setAndAddEndNode(CFGNode endNode) {
		this.endNode = endNode;
		addNode(endNode);
	}

	public void setAndAddAbnormalEndNode(CFGNode abnormalEndNode) {
		this.abnormalEndNode = abnormalEndNode;
		addNode(abnormalEndNode);
	}

	public CFGNode getStartNode() {
		return startNode;
	}
	
	public CFGNode getEndNode() {
		return endNode;
	}
	
	public CFGNode getAbnormalEndNode() {
		return abnormalEndNode;
	}
	
	@Override
	public CFGNodeType getCFGNodeType() {
		return CFGNodeType.N_SUB_CFG;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public String getMethodName() {
		return methodName;
	}

	public MethodDeclaration getMethod() {
		return method;
	}
	
	public String getClassName() {
		return className;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Test if the node is a virtual node
	 */
	public boolean isVirtual() {
		return false;
	}

	/**
	 * Test if the node is the start node of the entire CFG
	 */
	public boolean isStart() {
		return false;
	}
	
	/**
	 * Test if the node is the end node of the entire CFG
	 */
	public boolean isNormalEnd() {
		return false;
	}
	
	/**
	 * Test if the node is the abnormal end node of the entire CFG
	 */
	public boolean isAbnormalEnd() {
		return false;
	}
	
	/**
	 * Test if the node is a predicate node 
	 */
	public boolean isPredicate() {
		return false;
	}
	
	/**
	 * Write the (directed) graph to a text file, which can be regarded as the description of the graph 
	 * in dot language, and can be used to visualized the graph use Graphviz tools.
	 * @param out : the output text file, which should be opened
	 */
	
	public void simplyWriteToDotFile(PrintWriter output) throws IOException {
		final int MAX_LABEL_LEN = 300;
		
		String graphId = GraphUtil.getLegalToken(getId());
		output.println("digraph " + graphId + " {");
		for (GraphNode currentNode : nodes) {
			CFGNode node = (CFGNode)currentNode;
			
			String label = node.getDescription();
			if (label == null || label.trim().equals("")) label = node.getLabel();
			
			String nodeId = "node" + GraphUtil.getLegalToken(node.getId());
			String nodeShape = "box";
			// Set the special id and shape for start, normal and abnormal end, predicate and other virtual nodes
			if (node.isAbnormalEnd()) {
				nodeId = methodName + "_ABNORMAL_END";
				label = nodeId;
				nodeShape = "tripleoctagon";
			} else if (node.isNormalEnd()) {
				nodeId = methodName + "_END";
				label = nodeId;
				nodeShape = "octagon";
			} else if (node.isStart()) {
				nodeId = methodName + "_START";
				label = nodeId;
				nodeShape = "octagon";
			} else if (node.isVirtual()) {
				nodeShape = "hexagon";
			} else if (node.isPredicate()) {
				nodeShape = "diamond";
			}
			if (label.length() > MAX_LABEL_LEN) {
				label = label.substring(0, MAX_LABEL_LEN) + "...";
			}
			label = label.replace('\"', '\'');
			label = label.replace("\r", "");
			label = label.replace("\n", "");
			output.println("    " + nodeId + "[label = \"[" + node.getId() + "]" + label + "\", shape = " + nodeShape + "]");
			
		}
		for (GraphEdge edge : edges) {
			String label = edge.getLabel();
			CFGNode startNode = (CFGNode)edge.getStartNode();
			CFGNode endNode = (CFGNode)edge.getEndNode();
			
			String startNodeId = "node" + GraphUtil.getLegalToken(startNode.getId());
			String endNodeId = "node" + GraphUtil.getLegalToken(endNode.getId());
			
			// Set special id for start, normal and abnormal end nodes. These setting must be consistent with
			// the setting in the above loop for the nodes of the CFG
			if (startNode.isAbnormalEnd()) {
				startNodeId = methodName + "_ABNORMAL_END";
			} else if (startNode.isNormalEnd()) {
				startNodeId = methodName + "_END";
			} else if (startNode.isStart()) {
				startNodeId = methodName + "_START";
			}
			
			if (endNode.isAbnormalEnd()) {
				endNodeId = methodName + "_ABNORMAL_END";
			} else if (endNode.isNormalEnd()) {
				endNodeId = methodName + "_END";
			} else if (endNode.isStart()) {
				endNodeId = methodName + "_START";
			}

			if (label != null) {
				output.println("    " + startNodeId + "->" + endNodeId + "[label = \"" + label + "\"]");
			} else {
				output.println("    " + startNodeId + "->" + endNodeId);
			}
		}

		output.println("}");
		output.println();
		output.flush();
	}
	
	public void writeLiveVariablesToFile(PrintWriter output) throws IOException {
		final int MAX_LABEL_LEN = 300;
		
		String graphId = GraphUtil.getLegalToken(getId());
		output.println("digraph " + graphId + " {");
		for (GraphNode currentNode : nodes) {
			if (nodes instanceof ExecutionPoint) {
				System.out.println(((ExecutionPoint) currentNode).getLabel());
			}
			CFGNode node = (CFGNode)currentNode;
			
			String label = node.getDescription();
			if (label == null || label.trim().equals("")) label = node.getLabel();
			
			String nodeId = "node" + GraphUtil.getLegalToken(node.getId());
			String nodeShape = "box";
			// Set the special id and shape for start, normal and abnormal end, predicate and other virtual nodes
			if (node.isAbnormalEnd()) {
				nodeId = methodName + "_ABNORMAL_END";
				label = nodeId + label;
				nodeShape = "tripleoctagon";
			} else if (node.isNormalEnd()) {
				nodeId = methodName + "_END";
				label = nodeId + label;
				nodeShape = "octagon";
			} else if (node.isStart()) {
				nodeId = methodName + "_START";
				label = nodeId + label;
				nodeShape = "octagon";
			} else if (node.isVirtual()) {
				nodeShape = "hexagon";
			} else if (node.isPredicate()) {
				nodeShape = "diamond";
			}
			
			if (node instanceof ExecutionPoint) {
				ExecutionPoint graphNode = (ExecutionPoint)node;
				label += graphNode.getLabel();
			}
			
			if (label.length() > MAX_LABEL_LEN) {
				label = label.substring(0, MAX_LABEL_LEN) + "...";
			}
			label = label.replace('\"', '\'');
			label = label.replace("\r", "");
			output.println("    " + nodeId + "[label = \"[" + node.getId() + "]" + label + "\", shape = " + nodeShape + "]");
		}
		for (GraphEdge edge : edges) {
			String label = edge.getLabel();
			CFGNode startNode = (CFGNode)edge.getStartNode();
			CFGNode endNode = (CFGNode)edge.getEndNode();
			
			String startNodeId = "node" + GraphUtil.getLegalToken(startNode.getId());
			String endNodeId = "node" + GraphUtil.getLegalToken(endNode.getId());
			
			// Set special id for start, normal and abnormal end nodes. These setting must be consistent with
			// the setting in the above loop for the nodes of the CFG
			if (startNode.isAbnormalEnd()) {
				startNodeId = methodName + "_ABNORMAL_END";
			} else if (startNode.isNormalEnd()) {
				startNodeId = methodName + "_END";
			} else if (startNode.isStart()) {
				startNodeId = methodName + "_START";
			}
			
			if (endNode.isAbnormalEnd()) {
				endNodeId = methodName + "_ABNORMAL_END";
			} else if (endNode.isNormalEnd()) {
				endNodeId = methodName + "_END";
			} else if (endNode.isStart()) {
				endNodeId = methodName + "_START";
			}

			if (label != null) {
				output.println("    " + startNodeId + "->" + endNodeId + "[label = \"" + label + "\"]");
			} else {
				output.println("    " + startNodeId + "->" + endNodeId);
			}
		}

		output.println("}");
		output.println();
		output.flush();
	}
	
	//定值到达分析专用的writeToDot函数
	public void simplyWriteToDotFileFixedValue(PrintWriter output) throws IOException {
		final int MAX_LABEL_LEN = 1200;
		
		String graphId = GraphUtil.getLegalToken(getId());
		output.println("digraph " + graphId + " {");
		for (GraphNode currentNode : nodes) {
			CFGNode node = (CFGNode)currentNode;
			
			String label = node.getDescription();
			if (label == null || label.trim().equals("")) label = node.getLabel();
			
			String nodeId = "node" + GraphUtil.getLegalToken(node.getId());
			String nodeShape = "box";
			// Set the special id and shape for start, normal and abnormal end, predicate and other virtual nodes
			if (node.isAbnormalEnd()) {
				nodeId = methodName + "_ABNORMAL_END";
				label = nodeId;
				nodeShape = "tripleoctagon";
			} else if (node.isNormalEnd()) {
				nodeId = methodName + "_END";
				label = nodeId;
				nodeShape = "octagon";
			} else if (node.isStart()) {
				nodeId = methodName + "_START";
				label = nodeId;
				nodeShape = "octagon";
			} else if (node.isVirtual()) {
				nodeShape = "hexagon";
			} else if (node.isPredicate()) {
				nodeShape = "diamond";
			}
			label += "  ";

				if (currentNode instanceof ExecutionPoint) {
					ExecutionPoint eNode = (ExecutionPoint)currentNode;
					ReachNameRecorder recorder = (ReachNameRecorder)eNode.getFlowInfoRecorder();
					List<ReachNameDefinition> definedNameList = recorder.getReachNameList();
					int countAnalyze = 0;
					for (ReachNameDefinition definedName : definedNameList) {
						NameDefinition name = definedName.getName();
						NameReference value = definedName.getValue();
						countAnalyze++;
						if (definedName.getValue() != null) {
							label += "\n";
							label += "Name "+ countAnalyze +":    ";
							//output.println("在节点ID为" + "[" + graphNode.getId() + "]" + "的CFG节点\t" + "对" + name.getSimpleName() + "名字定义"+ "\t" + "使用" + value.toSimpleString() + "表达式来定值" + "\t[" + name.getLocation() + "]\t[" + value.getLocation() + "]");
							label += ("nodeID:   " + "[" + currentNode.getId() + "]" + "   name definition:   " + name.getSimpleName() + "   value:   " + value.toSimpleString() + "   nameLocation:   "+ "[" + name.getLocation() + "]" + "   valueLocation:   " + "[" +value.getLocation() + "]" +"\n");
						} else {
							label += "\n";
							label += "Name "+ countAnalyze +":    ";
							//output.println("[" + currentNode.getId() + "]\t" + definedName.getName().getSimpleName() + "\t~~\t[" + name.getLocation() + "]\t~~");
							label += ("nodeID:   " + "[" + currentNode.getId() + "]" + "   name definition:   " + definedName.getName().getSimpleName() + "   nameLocation:   " + "\t~~\t[" + name.getLocation() + "]\t~~"+"\n");
							//label += ("该名字定义无对应定值表达式\n\n");
						}
						//进行根源定值到达分析
						label += ("    ");
						label += ("Root Analysis: ");
						
						if(definedName.getValue() != null) {
							//buffer.append("以下是对上述到达定值进行根源到达定值分析，若为空说明对应到达定值已经是根源到达定值\n\n");
							int countRoot = 0;
							List<ReachNameDefinition> exploredNameList = ReachNameAnalyzer.getRootReachNameDefinitionList(this, eNode, value);
							for (ReachNameDefinition definedNameNew : exploredNameList) {
								NameDefinition nameNew = definedNameNew.getName();
								NameReference valueNew = definedNameNew.getValue();
								if(definedNameNew.getValue() != null) {
									countRoot++;
									label += ("root: ");
									label += (nameNew.getSimpleName()+valueNew.toSimpleString() +"nameLocation:   " + "["+nameNew.getLocation()+"]" + "valueLocation:   " + "["+valueNew.getLocation()+"]"+"\n"+"\n");
								} else {
									label += ("There is no corresponding value");
									//buffer.append("[" + graphNode.getId() + "]\t" + definedName.getName().getSimpleName() + "\t~~\t[" + name.getLocation() + "]\t~~"+"\n"+"\n");
								}
							}
							if(countRoot == 0) {
								label += ("It is root now");
							}
						
						}
						label += ("   ");
					}
					//buffer.append("基于该可执行点" + "("+ graphNode.getId() +")" +"的定值到达分析结束" + "\n" + "\n" + "\n" + "\n");
				}
			
			if (label.length() > MAX_LABEL_LEN) {
				label = label.substring(0, MAX_LABEL_LEN) + "...";
			}
			label = label.replace('\"', '\'');
			label = label.replace("\r", "");
			//label = label.replace("\n", "");
			output.println("    " + nodeId + "[label = \"[" + node.getId() + "]" + label + "\", shape = " + nodeShape + "]");
		
		}
		for (GraphEdge edge : edges) {
			String label = edge.getLabel();
			CFGNode startNode = (CFGNode)edge.getStartNode();
			CFGNode endNode = (CFGNode)edge.getEndNode();
			
			String startNodeId = "node" + GraphUtil.getLegalToken(startNode.getId());
			String endNodeId = "node" + GraphUtil.getLegalToken(endNode.getId());
			
			// Set special id for start, normal and abnormal end nodes. These setting must be consistent with
			// the setting in the above loop for the nodes of the CFG
			if (startNode.isAbnormalEnd()) {
				startNodeId = methodName + "_ABNORMAL_END";
			} else if (startNode.isNormalEnd()) {
				startNodeId = methodName + "_END";
			} else if (startNode.isStart()) {
				startNodeId = methodName + "_START";
			}
			
			if (endNode.isAbnormalEnd()) {
				endNodeId = methodName + "_ABNORMAL_END";
			} else if (endNode.isNormalEnd()) {
				endNodeId = methodName + "_END";
			} else if (endNode.isStart()) {
				endNodeId = methodName + "_START";
			}

			if (label != null) {
				output.println("    " + startNodeId + "->" + endNodeId + "[label = \"" + label + "\"]");
			} else {
				output.println("    " + startNodeId + "->" + endNodeId);
			}
		}

		output.println("}");
		output.println();
		output.flush();
	}
}
