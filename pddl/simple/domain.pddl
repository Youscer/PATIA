;; simple domain Typed version.
;;

(
  define (domain simple)
  (:requirements :strips :typing) 
  (:types physobj location - object
          picker package - physobj
  )
  
  (:predicates 
    (at ?phys - physobj ?loc - location)
		(in ?pkg - package ?pic - picker)
  )
  
  (:action LOAD-PICKER
   :parameters    (?pkg - package ?pic - picker ?loc - location)
   :precondition  (and (at ?pic ?loc) (at ?pkg ?loc))
   :effect        (and (not (at ?pkg ?loc)) (in ?pkg ?pic))
  )

  (:action UNLOAD-PICKER
    :parameters    (?pkg - package ?pic - picker ?loc - location)
    :precondition  (and (at ?pic ?loc) (in ?pkg ?pic))
    :effect        (and (at ?pkg ?loc) (not(in ?pkg ?pic)) )
  )

  (:action MOVE-PICKER
    :parameters    (?pic - picker ?locfrom - location ?locto - location)
    :precondition  (at ?pic ?locfrom)
    :effect        (and (not (at ?pic ?locfrom)) (at ?pic ?locto))
  )
)