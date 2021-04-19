(ns cassandra.simplify
  (:require
    [cassandra.AST :as AST :refer [leaf? left-child right-child children]]
    [cassandra.utils :as utils :refer [atom? third expt]]))

;; http://www.math.wpi.edu/IQP/BVCalcHist/calc5.html


;; 5.3.3 Transforming Negatives
(defn expand-unary-minus [root]
  (cond (atom? root) root
        (and (= (first root) '-)
               (AST/unary? root)) (list '* -1
                                         (expand-unary-minus (second root)))
        :else (cons (first root) (map expand-unary-minus (rest root)))))

(defn expand-minus [root]
  (declare _expand-minus)
  (defn negate [node]
    (list '* -1 (_expand-minus node)))
  (defn _expand-minus [root]
    (cond (atom? root) root
          (and (= (first root) '-)
               (AST/n-ary? root)) (concat '(+)
                                          (list (_expand-minus (second root)))
                                          (map negate (rest (rest root))))
          :else (cons (first root) (map _expand-minus (rest root)))))
  (_expand-minus root))


;; 5.3.4 Leveling Operators
(defn level [op]
  (fn rec [root]
    (cond (atom? root) root
           (= (first root) op) (cons (first root)
                                     (mapcat (fn [x]
                                                (if (and (coll? x)
                                                         (= (first x) op))
                                                  (rest (rec x))
                                                  (list x)))
                                              (rest root)))
          :else (cons (first root) (map rec (rest root))))))
(def level-add (level '+))
(def level-times (level '*))

;; 5.3.5 Simplifying Rational Expressions

(defn rational-simplify [root]
  (defn divide? [node]
    (and (coll? node)
         (= (AST/value node) '/)))
  (defn rec [root]
    (cond (atom? root) root
          (divide? root)
          (let [left (if (leaf? (left-child root))
                       (left-child root)
                       (cons (AST/value (left-child root))
                            (map rec (children (left-child root)))))
                right (if (leaf? (right-child root))
                        (right-child root)
                        (cons (AST/value (right-child root))
                             (map rec (children (right-child root)))))]
            (cond (and (divide? left)
                        (divide? right))
                  (list '/ (list '* (second left) (third right))
                       (list '* (third left) (second right)))

                  (divide? left)
                  (list '/ (left-child left) (list '* (right-child left) right))

                  (divide? right)
                  (list '/ (list '* left (right-child right)) (left-child right))

                  :else (list '/ left right)))
          :else (cons (first root) (map rec (rest root)))))
  (rec root))

(defn filter-zero-product [root]
  (cond
    (leaf? root) root
    (= (AST/value root)
           '*)
    (let [child (map filter-zero-product (children root))]
      (if (some true? (map #(= % 0)
                               child))
        0
        (cons (AST/value root) child)))
    :else (cons (AST/value root) (map filter-zero-product (children root)))))


(defn filter-identities [root]
  (defn splice-val [root]
    (if (leaf? root) root
      (cond (= (AST/value root)
               '*) (cond (= (count root) 1) 1
                         (= (count root) 2) (left-child root)
                         :else root)
            (= (AST/value root)
               '+) (cond (= (count root) 1) 0
                         (= (count root) 2) (left-child root)
                         :else root)
            :else root)))
  (defn rec [root]
    (if (leaf? root)
      root
      (let [child (map rec (children root))]
        (cons (AST/value root)
              (map splice-val (filter (cond (= (AST/value root) '+) #(not= % 0)
                                        (= (AST/value root) '*) #(not= % 1)
                                        :else #(or true %))
                                  child))))))
  (rec root))
(defn partial-evaluate [root]
  (cond (leaf? root) root
        (= (AST/value root) 'expt) (let [base (partial-evaluate (left-child root))
                                         power (partial-evaluate (right-child root))]
                                     (if (and (number? power)
                                              (number? base))
                                       (expt base power)
                                       (list 'expt base power)))
        (= (AST/value root) '/) (let [left (partial-evaluate (left-child root))
                                         right (partial-evaluate (right-child root))]
                                  (if (and (number? left)
                                           (number? right))
                                    (/ left right)
                                    (list '/ left right)))
        :else (let [op (AST/value root)
                    child (map partial-evaluate (children root))
                    numerical-child (apply (eval op) (filter #(and (leaf? %) (number? %))
                                              child))
                    other-children (filter #(not (and (leaf? %) (number? %)))
                                           child)]
                (if (empty? other-children)
                  numerical-child
                  (cons op (cons numerical-child other-children))))))
(defn distribute-power [root]
  (if (leaf? root) root
    (if (and (= (AST/value root) 'expt)
             (not (leaf? (left-child root)))
             (= (AST/value (left-child root)) '*))
      (AST/make-node '*
                     (map #(AST/make-node 'expt (list % (right-child root)))
                          (children (left-child root))))
      (AST/make-node (AST/value root) (map distribute-power (children root))))))

(def clean
  (comp filter-identities filter-zero-product partial-evaluate))
(def pprocess
  (comp rational-simplify level-times level-add  expand-minus expand-unary-minus))
(def simplify
  (comp clean pprocess))
