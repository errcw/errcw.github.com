---
layout: post
title: Naive Knight's Tour in Haskell
---
So tonight I was helping a friend with a CS 134 assignment that involved a
recursive solution to the [Knight's
Tour](http://en.wikipedia.org/wiki/Knight%27s_Tour) problem. The assignment
included a rather ugly solution in Java and I wondered what a comparable
solution in Haskell might look like. I spent the past few minutes hacking
together such a function.

    {% highlight haskell %}
    tourTo :: Int -> (Int,Int) -> [[(Int,Int)]]
    tourTo n finish = [pos:path | (pos,path) <- tour (n*n)]
        where tour 1 = [(finish, [])]
              tour k = [(pos', pos:path) |
                           (pos, path) <- tour (k-1),
                           pos' <- (filter (`notElem` path) (jumps n pos))]
              jumps n (r, c) = filter onBoard
                  [(r+2, c+1), (r+1, c+2), (r-2, c+1), (r+1, c-2),
                   (r+2, c-1), (r-1, c+2), (r-2, c-1), (r-1, c-2)]
                  where onBoard (r, c) = r >= 1 && c >= 1 && r <= n && c <= n
    {% endhighlight %}

It works (as far as I can tell) but it's unbearably slow. Two optimizations
come to mind: using a map rather than a list to store the visited squares and
using a heuristic to select the next move location. The latter is implemented
in the Java version; if motivation strikes I'll post a
better/faster(/harder/stronger) version.
