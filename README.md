# About the project 

Language processor and interpreter of a simple pseudocode language.

For example, the compiler takes the following program
```
@ Declaration section
int [10] list;
int i;
int a;
int j;
@ Actions
in a; 
if a > 0 then
	i = 0;
	while i < 10 do
		in list[i];
		i = i + 1;
	endwhile;
else	
	j = 0;
	while j < 10 do
		list[j] = 0;
		out list[j];
		j = j + 1;
	endwhile;
endif;
```

and performs: 

1. Lexical analysis by tokenizing the input program into valid elements of the language grammar.
2. Sintactical analysis by validating the grammar using a parse tree.

As a result, virtual machine code that can be executed by the interpreter module provided in the project is generated.
                          

