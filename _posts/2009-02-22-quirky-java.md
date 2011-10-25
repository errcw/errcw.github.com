---
layout: post
title: Quirky Java
---
This term I'm taking a course on compiler construction, [CS
444](http://www.student.cs.uwaterloo.ca/~cs444/). For the course I, along with
my group members [Peter](http://www.eng.uwaterloo.ca/~pfeiner/) and
[Ian](http://www.student.cs.uwaterloo.ca/~i3stewar/), am developing a compiler
for a subset of Java. So far we have finished the
[scanner](http://en.wikipedia.org/wiki/Lexical_analyzer) and
[parser](http://en.wikipedia.org/wiki/LALR). It's been an illuminating
experience learning about the quirkiness hidden deep in the [Java Language
Specification](http://java.sun.com/docs/books/jls/) and I wanted to share a few
of the more esoteric constructs we discovered.

I successfully compiled all of the following examples on my aging Windows XP
laptop with javac 1.6.0_04. First, a single semicolon is valid Java file! So

    {% highlight java %}
    // in Quirky.java
    ;
    {% endhighlight %}

is a perfectly valid and so is

    {% highlight java %}
    // in Quirky.java
    ;
    class Quirky {
        ;
    }
    {% endhighlight %}

Javac will happily generate bytecode for both files. More single-character
strangeness: a single $ or _ are valid Java identifiers. Hence, with a wanton
willingness to abuse the language,

    {% highlight java %}
    // in $.java
    class $ {
        class $$ {
            $ $($ $) { return null; }
        }
    }
    class _ {
        _ _(_ _) { return null; }
    }
    {% endhighlight %}

can be compiled into $.class, $$$$.class and \_.class! Finally the array
brackets can appear in unexpected places as

    {% highlight java %}
    // in Quirky.java
    class Quirky {
        int[] twoDim() [] { return null; }
    }
    {% endhighlight %}

generates a method twoDim that returns a two-dimensional array int\[\]\[\].
