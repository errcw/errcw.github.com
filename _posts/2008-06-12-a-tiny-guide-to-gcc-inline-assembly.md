---
layout: post
title: A Tiny Guide to GCC Inline Assembly
---
So I'm working on a real-time operating system for school, and in the process
I've needed to write a ton of IA32 inline assembly. GCC's inline assembly
syntax isn't immediately straightforward so it's been an interesting process of
trial, error, and documentation to piece together the specifics. This guide
presents my accumulated knowledge on the subject.


## Assembly Syntax

GCC uses AT&T assembly syntax. The highlights:

* __instruction source, destination__  
  The first operand is the source, the second is the destination.
* __%register__  
  Register names are prefixed with a percent sign. (Or a `%%` in certain
  circumstances; see the second on operands below.)
* __$literal__  
  Literal values are prefixed with a dollar sign. The literal `$10` specifies
  decimal 10 while `$0x10` specifies hexadecimal 16.
* __instruction{b,w,l}__  
The instruction suffix denotes the operand size. The `b`, `w`, and `l` specify byte (8-bit), word (16-bit), and long word (32-bit) memory references. (Always include the size! If you omit it the GNU assembler will attempt to guess for you which is usually a Bad Idea.)
* __segment:offset(base, index, scale)__  
Memory access syntax. Note that the offset and scale constants are *not* prefixed with `$` but the register references still need a `%`.
* __ljmp/lcall $segment, $offset__  
  Control transfer instructions may be prefixed with an `l` to indicate a far jump
  to another code segment. (Similarly, there is `lret $stackadjust`.)
* __\*branch-address__  
  Branch addressing using literals or registers is prefixed with an asterisk.

Here are a few examples of valid code that illustrate these points.

    {% highlight gas %}
    pushl %eax
    movl $8, %ebx
    movb $0x11, %al
    movl %es:16(%ebx, %edi, 4), %eax
    ret *100
    jmp *%ecx
    lcall $0x10, farcalllabel
    {% endhighlight %}
  

## Inline Syntax

The basic format for GCC inline assembly is as follows.

    {% highlight c %}
    __asm__
    __volatile__      /* optional */
    (
    assembly code
    : output operands /* optional */
    : input operands  /* optional */
    : clobber list    /* optional */
    );
    {% endhighlight %}

For example, below is code to turn on bit 1 in `flag` then store the value in `new_flag`.

    {% highlight c %}
    int flag, new_flag;
    __asm__
    (
    "movl %1, %%eax \n"
    "orw $2, %%ax \n"
    "movl %%ax, %0 \n"
    : "=r"(new_flag) /* output */
    : "r"(flag)      /* input */
    : "%eax"         /* clobbered register */
    );
    {% endhighlight %}

#### Preamble

The `__asm__` keyword marks the start of the inline assembly statement. While
using `asm` without the underscores is also valid in some contexts, it will not
compile with the `-std=c99` option. Moreover, the underscores prevent conflicts
with `asm` defined elsewhere in your code.

The optional `__volatile__` keyword indicates the assembly code has important
side-effects and guarantees GCC will not delete it if it is reachable. It does
not, however, guarantee that the assembly code will not be moved relative to
other code.

#### Code

The assembly code specifies the instructions to execute. Each instruction (or
label) is enclosed within double quotes and terminated by a newline.

#### Operands

The general pattern for an operand is `"constraint"(expression)` and multiple
operands are separated by commas.

In the assembly code each operand is reference by number, where `%0` is the
first output operand, `%1` is the second, and so on, and `%N-1` is the last
input operand. Because the operands are indicated by a percent sign the
register names must now be prefixed with two percent signs, like `%%eax`.

C expressions provide the input and output operands for the assembly code. An
output expression (an lvalue) specifies where a result should be stored. An
input expression specifies either a location (lvalue) or value (rvalue) as
input to the code.

Constraints help to decided the addressing mode and registers used for the
input and output operands. Of the many constraints available, only a few
are used frequently. These we discuss below.

* __m__: The operand is stored in memory, at any memory address. (Instructions
  will operate on the data directly in memory.)
* __r__: The operand is stored in a general-purpose register. (GCC generates
  code to transfer the operand to or from memory and the register it chooses.)
* __i__: The operand is an immediate integer.
* __0,...,9__: The operand matches the operand with the specified number. (GCC
  will use the same variable for both operands. The two operands that match
  must be one input-only operand and one output-only operand.)

Constraints may also have modifiers which provide additional control over the
behavior of the operands. Three common constraints are:

* __=__: Operand is write-only
* __+__: Operand is both read and written
* __&__: Operand is clobbered early (i.e., is modified before the instruction
  is finished using the input operands, meaning it may not lie in a register
  used as an input operand or any part of memory)

#### Clobber List

The clobber list should contain:

* The registers modified, either explicitly or implicitly, by your code.
* If your code modifies the condition code register, "cc".
* If your code modifies memory, "memory".

The clobber list informs GCC of the state potentially changed by your code so
it won't make incorrect assumptions about the state and break things (always a
Bad Thing).

## Examples ##

To further illustrate all the stuff stuffed into this guide, I've pulled a few
examples from my operating system.

To load the interrupt descriptor table register:

    {% highlight c %}
    void set_idt (idt_pointer_t *ptr) {
        __asm__ __volatile__ (
        "lidt %0 \n" : : "m"(ptr) );
    }
    {% endhighlight %}

To set the kernel code segment:

    {% highlight c %}
    void set_kcs () {
        __asm__ __volatile__ (
        "ljmp %0, $farjmp \n"
        "farjmp: \n"
        "nop \n"
        : "i"(KERNEL_SEG_CODE) );
    }
    {% endhighlight %}

To move bytes:

    {% highlight c %}
    void kcopy (unsigned int src, unsigned int dst,
                unsigned int nbytes) {
        __asm__ __volatile__ (
        "cld \n"
        "rep \n"
        "movsb \n"
        :
        : "S"(src), "D"(dst), "c"(nbytes)
        : "%esi", "%edi", "%ecx" );
    }
    {% endhighlight %}

## References

I pulled this information from a variety of sources, chief among them:

* [GCC Extended Asm manual](http://gcc.gnu.org/onlinedocs/gcc-4.3.0/gcc/Extended-Asm.html#Extended-Asm)
* [GNU as manual](http://sourceware.org/binutils/docs-2.18/as/index.html)
* [GCC-Inline-Assemby-HOWTO](http://www.ibiblio.org/gferg/ldp/GCC-Inline-Assembly-HOWTO.html)
* [AT&T Assembly Syntax](http://sig9.com/articles/att-syntax)

