---
layout: post
title: Powers of Four in C#
---
I recently implemented a function in C# to check if an unsigned integer is a
power of four as a part of my new game and I wanted to share two possible
implementations. The first relies on the fact that a power of four (1) has only
a single bit set and (2) has an even number of zero bits after the one.

    {% highlight csharp %}
    public static bool IsPowerOfFour(uint n)
    {
        if (n != 0 && (n & (n - 1)) == 0) // single bit set
        {
            int count = 0;
            while (n > 1)
            {
                n >>= 1;
                count += 1;
            }
            return (count % 2) == 0 ? true : false; // even zero bits
        }
        return false;
    }
    {% endhighlight %}

For 32-bit unsigned integers we can employ some bit manipulation to acheive the
same effect, by checking that we have a power of two that is also a power of
four.

    {% highlight csharp %}
    public static bool IsPowerOfFour(uint n)
    {
        return (n & (n - 1)) == 0 && (n & 0x5555555) != 0;
    }
    {% endhighlight %}
