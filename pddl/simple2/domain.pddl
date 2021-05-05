;; simple2 domain Typed version.
;;

(
  define (domain simple2)
  (:requirements :strips :typing) 
  (:types case robot - object
  )
  
  (:predicates 
    (at ?r1 - robot ?c1 - case)
  )
  
  (:action MOVE
   :parameters    (?r1 - robot ?cFrom - case ?cTo - case)
   :precondition  (and (at ?r1 ?cFrom))
   :effect        (and (not (at ?r1 ?cFrom)) (at ?r1 ?cTo))
  )

)