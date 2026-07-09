import sbt.*

object Compat {
  extension (org: String) {
    def %%%(n: String) = org %% n
  }
}
