package common

/**
 * Additional functionality for printing.
 */

object Printing {

	/**
	 * Writes a list of values (of type `Any`) in a manner directed by the parameters.
	 *
	 * @param indent how much to indent each item, only relevant if `sameLine` = false.
	 * @param sep the separator between items, usually "", " ", or ",".
	 * @param sameLine true iff. items are printed on the same line.
	 * @param list the list of items to be printed.
	 * @return the formatted string.
	 *
	 * Examples:
	 *
	 * Supposed we print the list: List(1,2,3).
	 *
	 * With indent=0 sep=' ' sameLine=false (used for monitors):
	 * 1
	 *
	 * 2
	 *
	 * 3
	 *
	 * With indent=0 sep=' ' sameLine=true (used for modifiers):
	 * 1 2 3
	 *
	 * With indent=0 sep=',' sameLine=true (used for formals, conditions, actions, and constraints):
	 * 1,2,3
	 *
	 * With indent=2 sep='' sameLine=false (used for transitions):
	 * 1
	 * 2
	 * 3
	 **/

  def list2String(indent : Int, sep : String, sameLine : Boolean)(list : List[Any]) : String = {
  	  var result : String = ""
  	  	var s = ""
  	  	var nl = ""
  	  	for (elem <- list) {
  	  		result += s
  	  		if (!sameLine) result += nl * (if (sep.length == 0) 1 else 2)
  	  		result += "  " * indent
  	  		result += elem
  	  		s = sep
  	  		nl = "\n"
  	  	}
  	  	return result
  }

}
