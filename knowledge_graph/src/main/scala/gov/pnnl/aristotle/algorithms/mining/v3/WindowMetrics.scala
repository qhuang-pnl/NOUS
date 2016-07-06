/**
 *
 * @author puro755
 * @dJul 6, 2016
 * @knowledge_graph
 */
package gov.pnnl.aristotle.algorithms.mining.v3

import java.io.Serializable
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import java.io.PrintWriter
import java.io.File


/**
 * @author puro755
 *
 */
class WindowMetrics extends Serializable {

  var pattern_in_this_winodw : RDD[(String, List[(Int,Long)])] = null
  var pattern__node_association_window : RDD[(String, Set[String])] = null
  var node_pattern_association_window : RDD[(String, Set[String])] = null

  def updateWindowMetrics(batch_metrics : BatchMetrics)
  {
    val batch_id = batch_metrics.id

    if(pattern_in_this_winodw!=null)
    {
    	    val joined_pattern_metrics = pattern_in_this_winodw.rightOuterJoin(batch_metrics.pattern_in_this_batch)
    this.pattern_in_this_winodw = joined_pattern_metrics.map(pattern 
        => (pattern._1, pattern._2._1.getOrElse(List.empty) ++ List((batch_id, pattern._2._2))))
    }else
    {
      this.pattern_in_this_winodw = batch_metrics.pattern_in_this_batch.map(pattern => (pattern._1,List((batch_id,pattern._2))))
    }

    if(pattern__node_association_window!=null)
    {
    	    val join_node_metrics = pattern__node_association_window.rightOuterJoin(batch_metrics.pattern__node_association)
    this.pattern__node_association_window = join_node_metrics.map(node 
        => (node._1, node._2._1.getOrElse(Set.empty) ++ node._2._2))
    }else
    {
      this.pattern__node_association_window = batch_metrics.pattern__node_association
    }
    
    
    if(node_pattern_association_window!=null)
    {
    	    val join_node_pattern_metrics = node_pattern_association_window.rightOuterJoin(batch_metrics.node_pattern_association)
    this.node_pattern_association_window = join_node_pattern_metrics.map(node 
        => (node._1, node._2._1.getOrElse(Set.empty) ++ node._2._2))
    }else
    {
      this.node_pattern_association_window = batch_metrics.node_pattern_association
    }
  }
  
  
  def saveWindowMetrics()
  {
    pattern_in_this_winodw.saveAsTextFile("WindowPatternSummary" + System.nanoTime())
    val writer_pattern = new PrintWriter(new File("WindowGraphMiningPattern.txt"))
    pattern__node_association_window.collect.foreach(p => writer_pattern.println(p._1 + ":" + p._2))
    writer_pattern.flush()
    pattern__node_association_window.saveAsTextFile("WidnowPatternNodeAssociation" + System.nanoTime())
    node_pattern_association_window.saveAsTextFile("WindowNodePatternAssociation" + System.nanoTime())
  }
}