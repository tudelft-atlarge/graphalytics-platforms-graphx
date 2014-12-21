package org.tudelft.graphalytics.graphx.bfs

import org.tudelft.graphalytics.graphx.GraphXPregelJob
import org.tudelft.graphalytics.GraphFormat
import org.apache.spark.graphx.{EdgeTriplet, VertexId}
import org.tudelft.graphalytics.algorithms.BFSParameters
import org.apache.spark.graphx.Graph

/**
 * The implementation of BFS on GraphX.
 *
 * @param graphPath the input path of the graph
 * @param graphFormat the format of the graph data
 * @param outputPath the output path of the computation
 * @param parameters the graph-specific parameters for BFS
 * @author Tim Hegeman
 */
class BreadthFirstSearchJob(graphPath : String, graphFormat : GraphFormat,
		outputPath : String, parameters : Object)
		extends	GraphXPregelJob[Long, Int, Long](graphPath, graphFormat, outputPath) {

	val bfsParam : BFSParameters = parameters match {
		case p : BFSParameters => p
		case _ => null
	}
	
	/**
	 * The BFS job requires a non-null parameters object of type BFSParameters.
	 * 
	 * @return true iff the input is valid
	 */
	def hasValidInput = bfsParam match {
		case null => false
		case _ => true
	}
	
	/**
	 * Preprocess the parsed graph (with default vertex and edge values) to a
	 * graph with correct initial values.
	 * 
	 * @param graph input graph
	 * @return preprocessed graph
	 */
	def preprocess(graph : Graph[Boolean, Int]) =
		graph.mapVertices((vid, _) => getInitialValue(vid))
		
	/**
	 * For BFS the source vertex has initial distance zero, all other vertices
	 * start at positive infinity.
	 * 
	 * @param vertexId ID of a vertex
	 * @return the initial value corresponding with vertexID
	 */
	def getInitialValue(vertexId : VertexId) =
		if (vertexId == bfsParam.getSourceVertex)
			0L
		else
			Long.MaxValue
	
	/**
	 * Pregel vertex program. Computes a new vertex value based for a given
	 * vertex ID, the old value of the vertex, and aggregated messages.
	 * 
	 * For BFS the new value (distance from the source vertex) is the minimum
	 * of the current value and the smallest incoming message.
	 * 
	 * @return the new value of the vertex
	 */
	def vertexProgram = (vertexId : VertexId, oldValue : Long, message : Long) =>
		math.min(oldValue, message)
	
	/**
	 * Pregel message generation. Produces for each edge a set of messages.
	 * 
	 * For BFS a message (a distance to the destination vertex) is only sent if
	 * the new distance is shorter than the distance already stored at the destination
	 * vertex.
	 * 
	 * @return a set of messages to send
	 */
	def sendMsg = (edgeData: EdgeTriplet[Long, Int]) =>
		if (edgeData.srcAttr < Long.MaxValue && edgeData.srcAttr + 1L < edgeData.dstAttr)
			Iterator((edgeData.dstId, edgeData.srcAttr + 1L))
		else
			Iterator.empty
	
	/**
	 * Pregel messasge combiner. Merges two messages for the same vertex to a
	 * single message.
	 * 
	 * For BFS the only relevant message is the one with the shortest distance from
	 * the source, so two messages can be combined by discarding the larger of the two.
	 * 
	 * @return the aggregated message
	 */
	def mergeMsg = (messageA : Long, messageB : Long) => math.min(messageA, messageB)
	
	/**
	 * @return initial message to send to all vertices
	 */
	def getInitialMessage = Long.MaxValue
	
	/**
	 * @return name of the GraphX job
	 */
	def getAppName = "Breadth-First Search"

	/**
	 * Convert a graph to the output format of this job.
	 * 
	 * For BFS the output format is one vertex per line, ID and value pair.
	 * 
	 * @return a RDD of strings (lines of output)
	 */
	def makeOutput(graph : Graph[Long, Int]) =
		graph.vertices.map(vertex => s"${vertex._1} ${vertex._2}")

}