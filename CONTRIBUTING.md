# Contributing

## Basic Guidelines

### Use an IDE.
Specifically, IntelliJ IDEA. Download the (free) Community Edition of it [here](https://www.jetbrains.com/idea/download/). Some people use other tools, like VS Code, but I would personally not recommend them for Java development.

### Always test your changes.
Do not submit something without at least running the game to see if it compiles.  
If you are submitting a new implementation, make sure it has a name and description and that it works properly.

## Style Guidelines

### Follow the formatting guidelines.
This means:
- No spaces around parentheses: `if(condition){`, `SomeType s = (SomeType)object`
- Same-line braces.
- 4 spaces indentation
- `camelCase`, **even for constants or enums**. Why? Because `SCREAMING_CASE` is ugly, annoying to type and does not achieve anything useful. Constants are *less* dangerous than variables, not more. Any reasonable IDE should highlight them for you anyway.
- No underscores for anything.
- Do not use braceless `if/else` statements. `if(x) statement else statement2` should **never** be done. In very specific situations, Parenthesis-free if statements are not allowed in a single line. `if(cond) return;` will not be allowed.
- Prefer single-line javadoc `/** @return for example */` instead of multiline javadoc whenever possible
- Short method/variable names (multipleLongWords should be avoided if it's possible to do so reasonably, especially for variables)
- Use wildcard imports - `import some.package.*` - for everything. This makes incorrect class usage more obvious (*e.g. com.github.dr.rwserver.util.Timer vs java.util.Timer*) and leads to cleaner-looking code.

### Use `struct` collections and classes when possible.
Instead of using `java.util.List`, `java.util.HashMap`, and other standard Java collections, use `Seq`, `ObjectMap` and other equivalents from `arc.struct`.
Why? Because that's what the rest of the codebase uses, and the standard collections have a lot of cruft and usability issues associated with them.  
In the rare case that concurrency is required, you may use the standard Java classes for that purpose (e.g. `CopyOnWriteArrayList`).  

What you'll usually need to change:
- `HashSet` -> `ObjectSet`
- `HashMap` -> `ObjectMap`
- `List` / `ArrayList` / `Stack` -> `Seq`
- *Many others*

### Avoid boxed types (Integer, Boolean)
Never create variables or collections with boxed types `Seq<Integer>` or `ObjectMap<Integer, ...>`. Use the collections specialized for this task, e.g. `IntSeq` and `IntMap`.


### Do not allocate anything if possible.
Never allocate `new` objects in the main loop. If you absolutely require new objects, use `Pools` to obtain and free object instances. 
Otherwise, use the `Tmp` variables for things like vector/shape operations, or create `static` variables for re-use.
If using a list, make it a static variable and clear it every time it is used. Re-use as much as possible.

### Avoid bloated code and unnecessary getters/setters.
This is situational, but in essence what it means is to avoid using any sort of getters and setters unless absolutely necessary. Public or protected fields should suffice for most things. 
If something needs to be encapsulated in the future, IntelliJ can handle it with a few clicks.


### Do not create methods unless necessary.
Unless a block of code is very large or used in more than 1-2 places, don't split it up into a separate method. Making unnecessary methods only creates confusion, and may slightly decrease performance.  

### Java can refer to alibaba's JavaDoc specification
