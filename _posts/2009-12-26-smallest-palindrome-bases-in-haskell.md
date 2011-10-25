---
layout: post
title: Smallest Palindrome Bases in Haskell
---
Everybody loves palindromes, right? I do, at least. That's why I was excited
when I learned that every number can be a palindrome when it's written in the
appropriate base. There is a trivial proof for this property: any number N > 3
is a palindrome in base N-1 because it may be written "11". So here is my
solution, in Haskell, to this [CodeChef
problem](http://www.codechef.com/problems/K2), that finds the smallest base
that makes any given number a palindrome.

    {% highlight haskell %}
    isPalindromeInBase :: Integer -> Integer -> Bool
    isPalindromeInBase value base = step leftmost 1
        where leftmost = base ^ floor(log(fromInteger value) / log(fromInteger base))
              step left right
                  | left <= right             = True
                  | digit left == digit right = step (left `div` base) (right * base)
                  | otherwise                 = False
              digit position = (value `div` position) `mod` base
    
    smallestPalindromeBase :: Integer -> Integer
    smallestPalindromeBase 1 = 2
    smallestPalindromeBase 2 = 3
    smallestPalindromeBase value = step 2
        where step base 
                  | base >= value || isPalindromeInBase value base = base
                  | otherwise                                      = step (base + 1)
    
    main = interact (unlines . map (show . smallestPalindromeBase . read) . tail . lines)
    {% endhighlight %}

This is certainly not the fastest solution possible. Indeed it is downright
naive. But hopefully the logic is clear.
