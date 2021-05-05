(define (problem simple2)
(:domain simple2)
(:objects
 r1 r2 - robot
 c1 c2 - case
)

(:init (at r1 c1) (at r2 c2))

(:goal (and (at r1 c2) (at r2 c2)) )
)