---
layout: post
title: Project Euler Problems 4-8
---
More Haskell, more [Project Euler](http://projecteuler.net/). Again I thought I
would share my solutions to the next few problems. I should point out in
advance that, for the most part, these are naive solutions. I plan on taking a
second pass later to devise more clever approaches.

#### Problem 4
    {% highlight haskell %}
    answer = maximum . filter isPalindrome $ products
        where products = [ x*y | x <- [111..999], y <- [x..999] ]
              isPalindrome n = n == reverse n 0
                where reverse 0 r = r
                      reverse n r = reverse (n `div` 10) (r * 10 + n `mod` 10)
    {% endhighlight %}

#### Problem 5
    {% highlight haskell %}
    answer = head . filter good $ [20,40..]
        where good n = not $ any (\t -> n `rem` t /= 0) [2..19]
    {% endhighlight %}

#### Problem 6
    {% highlight haskell %}
    answer = squareSum - sumSquare
        where squareSum = (sum [1..100]) ^ 2
              sumSquare = sum $ map (^2) [1..100]
    {% endhighlight %}

#### Problem 7
    {% highlight haskell %}
    primes :: [Integer]
    primes = 2:3:primes'
        where 1:p:candidates = [6*k+r | k <- [0..], r <- [1,5]]
              primes'        = p : filter isPrime candidates
              isPrime n      = all (not . divides n) $ takeWhile (\p-> p*p <= n) primes'
              divides n p    = n `mod` p == 0
    answer = primes !! 10001
    {% endhighlight %}

####Problem 8####
    {% highlight haskell %}
    import Char
    largestDigitProduct d n = maximum products
        where products = [ digitProduct x digits | x <- [0..995]]
              digitProduct t ds = product $ take d (drop t ds)
              digits = map digitToInt (show n)
    answer = largestDigitProduct 5 <number>
    {% endhighlight %}
