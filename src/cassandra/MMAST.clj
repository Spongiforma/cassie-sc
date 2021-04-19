(ns cassandra.MMAST
  (:require [cassandra.simplify :as simplify]
            [cassandra.calculus :as calculus]
            [cassandra.roots :as roots ]
            [cassandra.AST :refer [infix stringify]]
            [cassandra.utils :refer [expt]])
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
                          operation (second args)
                          arg1 (if (> (count args) 2)
                                 (read-string (nth args 2))
                                 nil)
                          arg2 (if (> (count args) 3)
                                 (read-string (nth args 3))
                                 nil)]
                      (println input)
                      (println operation)
                      (case operation
                        "simplify" (simplify/clean input)
                        "differentiate" (calculus/differentiate input 'x)
                        "newton-raphson" (roots/newton-raphson input 'x 0.01 3.555452)
                        "distribute-power" (simplify/distribute-power input)
                        "simpsons" (calculus/simpsons input 'x 10000 arg1 arg2)
                        "divide" (simplify/simplify (list '/ input arg1))
                        "times" (simplify/simplify (list '* input arg1))
                        "minus" (simplify/simplify (list '- input arg1))
                        "plus" (simplify/simplify (list '+ input arg1))
                        "expt" (simplify/simplify (list 'expt input arg1))
                        "foo" (roots/make-guess input 'x 5)
                        nil))))))