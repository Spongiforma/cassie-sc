(ns cassandra.AST
  (:require [cassandra.utils :as utils :refer [third expt]]))

(defn unary? [root]
  (= (count root) 2))
(def n-ary? (complement unary?))
(defn value [root]
  (first root))
(defn left-child [root]
  (second root))
(defn right-child [root]
  (utils/third root))
(defn children [root]
  (rest root))
(defn leaf? [root]
  (utils/atom? root))
(def make-node cons)
(defn constant? [root]
  (if (leaf? root)
    (number? (value root))
    (not-any? (map constant? (children root)))))
(defn substitute [root x val]
  (defn _substitute [root]
    (cond (= root x) val
          (leaf? root) root
          :else
          (make-node (value root) (map _substitute (children root)))))
  (_substitute root))
;; replaces x with x^1
(defn force-degree-node [root x]
  (if (leaf? root)
    root
    (make-node (value root) (if (= (value root) 'expn)
                              (cons (left-child root)
                                    (map #(if (= % x)
                                            `(expn ~x 1)
                                            %)
                                         (rest (children root))))
                              (map #(if (= % x)
                                      `(expn ~x 1)
                                      %)
                                   (children root))))))
(defn infix [root]
  (if (leaf? root)
    root
    (rest (mapcat (fn [x] [(value root) x])
                  (map infix (children root))))))

(defn stringify [root]
  (defn condense-space [str]
    (clojure.string/replace str "  " " "))
  (defn _stringify [root]
    (if (leaf? root)
      (case root
        * "\\times"
        expt "^"
        (pr-str root))
      (cond
        ;(= (second root)
        ;   '/) (format "\\frac{%s}{%s}" (stringify (first root)) (stringify (third root)))
        (= (second root)
           'expt) (format "%s^{%s}" (stringify (first root)) (stringify (third root)))
        :else (format "(%s)" (clojure.string/join " "
                                                  (map stringify root))))))
  (condense-space (_stringify root)))

