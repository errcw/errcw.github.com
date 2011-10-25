---
layout: post
title: Project Euler Problems 1-3
---
Tonight I've been using Haskell to tackle [Project Euler](http://projecteuler.net/). I thought I would share my solutions to the first three problems.

#### Problem 1
    {% highlight haskell %}
    naiveAnswer = sum [x | x <- [1..999], x `mod` 3 == 0 || x `mod` 5 == 0]
    betterAnswer = sumDiv 3 + sumDiv 5 - sumDiv 15
        where sumDiv n = sum [n,n+n..999]
    {% endhighlight %}

#### Problem 2
    {% highlight haskell %}
    fibs = 1 : 1 : zipWith (+) fibs (tail fibs)
    answer = sum . takeWhile (< 4000000) . filter even $ fibs
    {% endhighlight %}

#### Problem 3
    {% highlight haskell %}
    largestFactor :: Integer -> Integer
    largestFactor n = search n 1 2
        where search q f d | q == 1         = f
                           | q < d*d        = q
                           | q `mod` d == 0 = loop (q`div`d) d d
                           | otherwise      = loop q f (d+1)
    answer = largestFactor 600851475143
    {% endhighlight %}

I'd love to hear any comments on how Haskell-y these solutions are.
