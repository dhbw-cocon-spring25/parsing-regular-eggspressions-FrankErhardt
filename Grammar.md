# Grammar Regeggs language
| Produktion                                                        | First-Menge     | Follow-Menge      | SELECT          |
|-------------------------------------------------------------------| --------------- | ----------------- | --------------- |
| $\text{regex} \to \text{concat} \space \text{union}$              | $[, (, LITERAL$ | $),\$$            | $[, (, LITERAL$ |
| $\text{union} \to \space \mid \text{concat} \space \text{union}$  | $\mid$          | $), \$$           | $\mid$          |
| $\text{union} \to \epsilon$                                       | $\epsilon$      | $), \$$           | $), \$$         |
| $\text{concat} \to \text{kleene} \space \text{suffix}$            | $[, (, LITERAL$ | $\mid, ), \$$     | $[,(, LITERAL$  |
| $\text{suffix} \to \space \text{kleene} \space \text{suffix}$     | $[, (, LITERAL$ | $\mid, ), \$$     | $[,(, LITERAL$  |
| $\text{suffix} \to \epsilon$                                      | $\epsilon$      | $\mid, ), \$$     | $\mid, ), \$$   |
| $\text{kleene} \to \text{base} \space \text{star}$                | $[, (, LITERAL$ | $[, (, LITERAL, \mid, ), \$$     | $[,(, LITERAL$  |
| $\text{star} \to \text{*}$                                        | $*$             | $[, (, LITERAL, \mid, ), \$$     | $*$             |
| $\text{star} \to \epsilon$                                        | $\epsilon$      | $[, (, LITERAL, \mid, ), \$$     | $[, (, LITERAL,    \mid, ), \$$   |
| $\text{base} \to \text{LITERAL}$                                  | $LITERAL$       | $*, [, (, LITERAL, \mid, ), \$$  | $LITERAL$       |
| $\text{base} \to ( \text{regex} )$                                | $($             | $*, [, (, LITERAL, \mid, ), \$$ | $($             |
| $\text{base} \to [caret \space \text{chars} \space \text{range}]$ | $[$             | $*, [, (, LITERAL, \mid, ), \$$  | $[$             |
| $caret \to \hat \space$                                           | $\hat \space$   | $LITERAL$         | $\hat \space$   |
| $caret \to \epsilon$                                              | $\epsilon$      | $LITERAL$         | $LITERAL$       |
| $\text{range} \to \text{chars} \space \text{range}$               | $LITERAL$       | $]$               | $LITERAL$       |
| $\text{range} \to \epsilon$                                       | $\epsilon$      | $]$               | $]$             |
| $\text{chars} \to \text{LITERAL} \space \text{rest}$              | $LITERAL$       | $LITERAL, ]$      | $LITERAL$       |
| $\text{rest} \to \text{- LITERAL}$                                | $-$             | $LITERAL, ]$      | $-$             |
| $\text{rest} \to \epsilon$                                        | $\epsilon$      | $LITERAL, ]$      | $LITERAL, ]$    |