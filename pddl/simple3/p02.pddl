(define (problem simple3)
(:domain simple3)
(:objects
 r1 r2 - robot
 c1 c2 c3 - case
)

(:init (at r1 c1) (at r2 c3) 
(next c1 c2) (next c2 c3)
(next c2 c1) (next c3 c2)
)

(:goal (and (at r1 c3) (at r2 c1)))
)