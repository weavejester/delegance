(ns delegance.memory
  (:require [delegance.protocols :refer :all]))

(deftype MemoryStore [a]
  Store
  (store [_ val]
    (let [key (java.util.UUID/randomUUID)]
      (swap! a assoc key val)
      key))
  (fetch [_ key]
    (@a key)))

(defn memory-store []
  (MemoryStore. (atom {})))

(deftype MemoryQueue [q r]
  Queue
  (push [_ data]
    (dosync
     (let [key (java.util.UUID/randomUUID)]
       (alter q conj {:id key, :data data}))))
  (reserve [_]
    (dosync
     (let [val (peek @q)]
       (alter q pop)
       (alter r assoc (:id val) val)
       val)))
  (finish [_ key]
    (dosync
     (alter r dissoc key))))

(defn memory-queue []
  (MemoryQueue.
   (ref (clojure.lang.PersistentQueue/EMPTY))
   (ref {})))