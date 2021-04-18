(ns cassandra.utils)

(def atom? (complement coll?))
(defn third [x] (second (rest x)))

(def ^{:private true} minus (first [-' -]))
(def ^{:private true} mult (first [*' *]))
(defmacro when-available
  [sym & body]
  (try
    (when (resolve sym)
      (list* 'do body))
    (catch ClassNotFoundException _#)))
(defn- expt-int [base pow]
  (loop [n pow, y (num 1), z base]
    (let [t (even? n), n (quot n 2)]
      (cond
        t (recur n y (mult z z))
        (zero? n) (mult z y)
        :else (recur n (mult z y) (mult z z))))))
(defn expt
  "(expt base pow) is base to the pow power.
Returns an exact number if the base is an exact number and the power is an integer, otherwise returns a double."
  [base pow]
  (if (and (not (float? base)) (integer? pow))
    (cond
      (pos? pow) (expt-int base pow)
      (zero? pow) (cond
                    (= (type base) BigDecimal) 1M
                    (= (type base) java.math.BigInteger) (java.math.BigInteger. "1")
                    (when-available clojure.lang.BigInt (= (type base) clojure.lang.BigInt))
                    (when-available clojure.lang.BigInt (bigint 1))
                    :else 1)
      :else (/ 1 (expt-int base (minus pow))))
    (Math/pow base pow)))