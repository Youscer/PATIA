(define (problem simple)
(:domain simple)
(:objects
 pic1 - picker
 loc1 loc2 - location
 pckg1 pckg2 - package
)

(:init (at pic1 loc2) (at pckg1 loc1) (at pckg2 loc1))

(:goal (and (at pckg1 loc2) (at pckg2 loc2))))