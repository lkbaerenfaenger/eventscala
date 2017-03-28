package com.lambdarookie.eventscala.graph.nodes.traits

import akka.actor.ActorRef
import com.lambdarookie.eventscala.data.Events._
import com.lambdarookie.eventscala.data.Queries._
import com.lambdarookie.eventscala.graph.qos._

trait UnaryNode extends Node {

  override val query: UnaryQuery

  val createdCallback: Option[() => Any]
  val eventCallback: Option[(Event) => Any]

  val childNode: ActorRef = createChildNode(1, query.sq)

  val frequencyMonitor: UnaryNodeMonitor = frequencyMonitorFactory.createUnaryNodeMonitor
  val latencyMonitor: UnaryNodeMonitor = latencyMonitorFactory.createUnaryNodeMonitor
  val nodeData: UnaryNodeData = UnaryNodeData(name, query, context, childNode)

  def emitCreated(): Unit = {
    if (createdCallback.isDefined) createdCallback.get.apply() else context.parent ! Created
    frequencyMonitor.onCreated(nodeData)
    latencyMonitor.onCreated(nodeData)
  }

  def emitEvent(event: Event): Unit = {
    if (eventCallback.isDefined) eventCallback.get.apply(event) else context.parent ! event
    frequencyMonitor.onEventEmit(event, nodeData)
    latencyMonitor.onEventEmit(event, nodeData)
  }

}
