;; simple3 domain Typed version.
;;

(
  define (domain simple3)
  (:requirements :strips :typing) 
  (:types case robot - object
  )
  
  (:predicates 
    (at ?r1 - robot ?c1 - case)
    (next ?c1 - case ?c2 - case)
  )
  
  (:action MOVE
   :parameters    (?r1 - robot ?cFrom - case ?cTo - case)
   :precondition  (and (at ?r1 ?cFrom) (next ?cFrom ?cTo))
   :effect        (and (not (at ?r1 ?cFrom)) (at ?r1 ?cTo))
  )

)