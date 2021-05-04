(define (problem simple3)
(:domain simple3)
(:objects
 r1 - robot
 c1 c2 c3 c4 c5 - case
)

(:init (at r1 c1) (next c1 c2) (next c2 c3) (next c3 c4) (next c4 c5))

(:goal (at r1 c5))
)