package amanuensis.core.util

object StringUtils {
  def truncate(s: String, size: Int) = {
    if(s.length <= size) s
    else s.substring(0,size)
  }
}