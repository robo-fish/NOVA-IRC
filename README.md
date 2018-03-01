# Nova IRC

![Legacy Webpage](https://raw.githubusercontent.com/robo-fish/Nova-IRC/master/fish/robo/nova/images/nova_logo.png)

Nova is an [IRC](https://en.wikipedia.org/wiki/Internet_Relay_Chat) client that I created during 1999 when I was a graduate student at Chalmers in Sweden. Nova is implemented in Java. The code in this repository is based on the last released version, "1.5J2 alpha" from early 2000, where the suffix "J2" refers to the use of the Java 2 SDK, which was new at the time. Java 2 JDK was the first Java development kit where the *Swing* GUI toolkit was included. However, there were no generics in Java 2 yet. They were introduced in a later version of Java. So I changed the original code by adding the missing type specifications in order reduce the compiler warnings emitted by the Java 9 JDK. I was very surprised, actually, at how little work was needed to make Nova's source code build with JDK 9 and fix all warnings. Well done, Java API overlords!

Nova uses the [JavaHelp](https://github.com/javaee/javahelp) utility to display searchable help pages inside the app. The distributable jar file (jh.jar) for JavaHelp is included in the repository. Not just for convenience, also because JavaHelp is now obsolete. No surprise; a lot happened in the Java technology world since I stopped using Java as my main programming language. Sun Microsystems, the creator of Java, was acquired by Oracle and, after a period of decline as a language for front-end applications, Java had its big comeback as the official programming language for Android apps.

The [original webpage at SourceForge.net](http://nova-irc.sourceforge.net) is still online. Unfortunately, I lost the password for my old SourceForge account, and the associated email address has expired long ago, so there is no way of resetting the password. The page will remain as it was in the year 2000 until SourceForge finally removes it due to inactivity.



Here is the original release notice, in which I used my middle name:

    Documentation is out of date. Sorry. Had no time to write something
    properly. Just play around with the program. I'm sure you will discover
    all of its features very quickly. Thanks for trying the alpha version.

    Starter class: com.berk.nova.Nova

    Run "java com.berk.nova.Nova" while being in the root Nova directory.
    If you use JDK 1.1.x, don't forget to add the Swing GUI components to
    your classpath. If you have a Java 2 SDK you should be fine with that
    command.

    Berk Oezer
    http://www.etek.chalmers.se/~berk/

    June 29, 1999 (two days before my summer vacation starts)


I changed the namespace of the Volta classes from *com.berk* to *fish.robo*, applying the reverse domain name convention to my actually existing domain "robo.fish". The website domain name "berk.com" never existed, or, if it existed, there was no relationship between me and the domain owners.
