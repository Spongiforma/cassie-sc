(ns cassandra.MMAST
  (:require [cassandra.simplify :as simplify]
            [cassandra.calculus :as calculus]
            [cassandra.roots :as roots ]
            [cassandra.AST :refer [infix stringify]]
            [cassandra.simplify :as simplify]
            [clojure.math.numeric-tower :refer [expt]]))
  (:gen-class
    :name cassandra.MMAST
    :methods [#^{:static true} [evaluate [String String] String]]))

(defn -evaluate [input operation]
  (stringify (infix (case operation
              "simplify" (simplify/simplify (read-string input))
              "null"))))

(defn -main [& args]
  (println (stringify
             (infix (let [input (simplify/pprocess (read-string (first args)))
                    operation (second args)]
                (case operation
                  "simplify" (simplify/clean input)
                  "differentiate" (calculus/differentiate input 'x)
                  "newton-raphson" (roots/newton-raphson input 'x 0.01 3.555452)
                  "distribute-power" (simplify/distribute-power input)
                  "simpsons" (calculus/simpsons input 'x 100 (nth 2 args) (nth 3 args))
                  "divide" (simplify/simplify (/ input (nth 2 args)))
                  "times" (simplify/simplify (* input (nth 2 args)))
                  "minus" (simplify/simplify (- input (nth 2 args)))
                  "plus" (simplify/simplify (+ input (nth 2 args)))
                  nil))))))
