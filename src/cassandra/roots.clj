(ns cassandra.roots
  (:require
    [cassandra.AST :as AST]
    [cassandra.calculus :as calculus]))

(defn newton-raphson [root x diff seed]
  (defn make-guess [prev-guess]
    (- prev-guess
       (/ (eval (AST/substitute root x prev-guess))
          (eval (AST/substitute (calculus/differentiate root x) x prev-guess)))))
  (defn _nr [guess]
    (let [new-guess (make-guess guess)]
      (if (< (/ (Math/abs (- guess new-guess))
                guess)
             diff)
        new-guess
        (_nr new-guess))))
  (_nr seed))

;(newton-raphson '(+ x 3) 'x 0.1 3)
