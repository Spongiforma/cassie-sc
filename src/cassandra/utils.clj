(ns cassandra.utils)

(def atom? (complement coll?))
(defn third [x] (second (rest x)))
