(ns delegance.memory
  (:require [delegance.protocols :refer :all]
            [delegance.util :refer :all]))

(defn- current-time []
  (long (/ (System/currentTimeMillis) 1000)))

(defn- expired? [x]
  (if-let [expires (:expires x)]
    (>= (current-time) expires)))

(deftype MemoryQueue [queue reserved timeout]
  Queue
  (push [_ data]
    (dosync
     (alter queue conj {:id (random-uuid) :created (current-time) :data data})
     nil))
  (reserve [_]
    (dosync
     (doseq [[id x] @reserved :when (expired? x)]
       (alter reserved dissoc id)
       (alter queue conj (dissoc x :expires)))
     (if-let [val (peek @queue)]
       (let [val (assoc val :expires (+ (current-time) timeout))]
         (alter queue pop)
         (alter reserved assoc (:id val) val)
         [(:id val) (:data val)]))))
  (finish [_ job-id]
    (dosync
     (alter reserved dissoc job-id)
     nil)))

(defn memory-queue
  ([] (memory-queue 300))
  ([timeout]
     (MemoryQueue.
      (ref (clojure.lang.PersistentQueue/EMPTY))
      (ref {})
      timeout)))

(deftype MemoryState [a]
  State
  (get! [_ key]
    (@a key))
  (put [_ key val]
    (swap! a assoc key val))
  (modify* [_ key func]
    (swap! a update-in [key] func)))

(defn memory-state []
  (MemoryState. (atom {})))
