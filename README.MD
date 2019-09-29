Hi!

# How to package
cmd: `maven install` to get an executable jar in the `/target` folder 

The executable jar will be named `logmonitor-1.0-jar-with-dependencies.jar`

# How to run the executable jar you just generated
Go to the `/target` folder

cmd: `java -jar logmonitor-1.0-jar-with-dependencies.jar`

And voilà!

# How to run the project inside your IDE
Navigate to `src/main/java/io.karon.logmonitor/Main` and... run the `main(args)` :)

# But I want to test your project in action!
No worries!

Navigate to `src/test/java/io.karon.logmonitor/LogGenerator` and there's another `main(args)` commented out.

Uncomment it (and the associated imports), run it, and the log file will get flooded (depending on the period you set) by generated logs.

