package models

import java.util.concurrent.Executors
import javax.inject.Inject

import delight.nashornsandbox.NashornSandboxes
import jdk.nashorn.api.scripting.ScriptObjectMirror
import play.Configuration
import play.api.libs.json.JsValue

import scala.util.{Failure, Success, Try}

/**
  * a filter of messages.
  * evaluate a string containing javascript code to filter a json message.
  * The JavaScript must contain a function filter(msg)
  * where msg is the json message as a string.
  *
  * ref: https://github.com/javadelight/delight-nashorn-sandbox
  */
class FilterJsonMsg(val clientScript: String) {

  import FilterJsonMsg._

  // determine if the script can be executed
  var scriptOk = false

  // check the script
  Try(sandbox.eval(clientScript)) match {
    case Success(s) => scriptOk = true; logger.info(s"script: $s ")
    case Failure(x) => scriptOk = false; logger.error(s"could not evaluate script: $x ")
  }

  /**
    * return true if the msg has passed the filter or has errors, else return false
    *
    * @param msg the json msg as a string to test
    * @return true if the msg has passed the filter or has errors, else return false
    */
  def accept(msg: JsValue): Boolean = {
    // check the script can be executed
    if (scriptOk) {
      sandbox.get("filter").asInstanceOf[ScriptObjectMirror] match {
        // if have no function or some error return true
        case null => true

        case filtering =>
          try {
            // invoke the filter function with msg as argument, return the result as a boolean
            filtering.call(this, msg).asInstanceOf[Boolean]
          } catch {
            // return true on any error
            case ex: Throwable =>
              logger.error(s"-----> error in the filter function: \n $ex")
              true
          }
      }
    } else {
      // return true if the script is no good
      true
    }
  }
}

object FilterJsonMsg {
  val logger = org.slf4j.LoggerFactory.getLogger("models.FilterJsonMsg")
  @Inject() val conf = Configuration.root()
  // get the time limit for the script
  val cpuTime = conf.getLong("mikan.filter.cputime", 200L)
  // the sandbox by default blocks access to all Java classes
  val sandbox = NashornSandboxes.create
  // limiting the CPU time of the script
  sandbox.setMaxCPUTime(cpuTime)
  sandbox.setExecutor(Executors.newSingleThreadExecutor)
}