package common

/**
 * Various utility functions.
 */

object Util {
	/**
	 * Converts a set to a string.
	 *
	 * @param set the set to be converted.
	 * @param braces true if braces `{` '}'should be inserted around the set.
	 * @tparam T the type of set elements.
	 * @return the set in string format.
	 */

	def setToString[T](set : Set[T], braces : Boolean = false) : String = {
		var result = ""
		if (braces) result += "{"
		var comma = ""
		for (elem <- set) {
			result += comma + elem
			comma = ","
		}
		if (braces) result += "}"	
		result
	}

	/**
	 * Prints a message surrounded by lines.
	 *
	 * @param ch the character making up the lines.
	 * @param msg the message.
	 */

	def headline(ch : String, msg : String) {
		val length = msg.length
		val line = ch * length
		println()
		println(line)
		println(msg)
		println(line)
		println()
	}

	/**
	 * Prints a message surrounded by lines made up of '-'.
	 *
	 * @param msg the message to be printed.
	 */

	def headline(msg : String) {
		headline("-",msg)
	}

	/**
	 * Prints the LogScope banner in ASCII art format.
	 * ASCII banners: http://www.network-science.de/ascii.
	 */

	def bannerLogScope() {
		println("""

 _                ____                       
| |    ___   __ _/ ___|  ___ ___  _ __   ___ 
| |   / _ \ / _` \___ \ / __/ _ \| '_ \ / _ \
| |__| (_) | (_| |___) | (_| (_) | |_) |  __/
|_____\___/ \__, |____/ \___\___/| .__/ \___|
            |___/                |_|         
		                                          
	""")
	}
}
