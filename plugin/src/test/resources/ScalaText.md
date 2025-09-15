This is an example of tests in markdown text.

Code blocks start with ````scala` are treated as tests. For example,

```scala
// Example of an example test
scala> List.tabulate(10)(identity).foldLeft(0)(_ + _)
res0: Int = 45

// Example of a property based test
prop> (i: Int) => i + i + i == i * 3
```
