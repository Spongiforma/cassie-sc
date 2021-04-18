(ns cassandra.calculus
  (:require
    [cassandra.AST :as AST :refer [leaf? left-child right-child children]]
    [cassandra.utils :as utils :refer [atom? third]]
    [cassandra.simplify :as simplify]))

(defn differentiate [root x]
  (defn drop-nth [coll n]
    (concat
      (take n coll)
      (drop (inc n) coll)))
  (simplify/simplify (cond (= 'x root) 1
         (leaf? root) 0
         :else (let [nchildren (map differentiate (children root) (repeat (count (children root))
                                                                          x))]
                 (cond
                   (= (AST/value root) '+) (cons (AST/value root) nchildren)
                   (= (AST/value root) '*) (cons '+
                                                 (map-indexed
                                                   (fn [idx y]
                                                     (cons '*
                                                           (cons y
                                                                 (drop-nth (children root) idx))))
                                                   nchildren))
                   ;; assuming exponent is constant
                   (= (AST/value root) 'expt) (cons '* (list
                                                         (cons '* (list (right-child root)
                                                                        (cons 'expt (list (left-child root)
                                                                                          (cons '- (list (right-child root)
                                                                                                         1))))))
                                                         (differentiate (left-child root) x))))))))
(defn simpsons [root x n a b]
  (let [h (/ (float (- b a)) n)
        f #(eval (AST/substitute root x %))]
    (/
      (* h (+ (* 4 (apply + (map #(f (+ a (* % h)))
                                 (filter even? (range 2 n)))))
              (* 2 (apply + (map #(f (+ a (* % h)))
                                 (filter odd? (range 1 n)))))))
      3)))

