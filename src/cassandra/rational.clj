(ns cassandra.rational
  (:require
    [cassandra.AST :as AST :refer [leaf? left-child right-child children]]
    [cassandra.utils :as utils :refer [atom? third]]))

(defn degree [poly x]
  (if (leaf? poly)
    (if (= poly x)
      1
      0)
    (if (and (= (AST/value poly)
                'expn)
             (= (left-child poly)
                x))
      (eval (right-child poly))
      (apply max (map degree (children poly)
                      (repeat (count (children poly)) x))))))

(defn polynomial? [poly x]
  (defn _polynomial? [poly in-exponent?]
    (cond (and (leaf? poly)
               in-exponent?
               (= (AST/value poly)
                  x)) false
          (leaf? poly) true
          (= (AST/value poly)
               'expn) (_polynomial? (right-child poly) true)
            :else (every? #(_polynomial? % in-exponent?)
                          (children poly))))
  (_polynomial? poly false))
