(ns delegance.memory
  "An implementation of the Delegance protocols for an in-memory queue and
  state. Useful for testing and debugging purposes."
  (:require [delegance.protocols :refer :all]))

(defn- current-time []
  (long (/ (System/currentTimeMillis) 1000)))

(defn- expired? [x]
  (if-let [expires (:expires x)]
    (>= (current-time) expires)))

(deftype MemoryQueue [queue reserved timeout]
  Queue
  (push [_ data]
    (dosync
     (let [job-id (java.util.UUID/randomUUID)]
       (alter queue conj {:id job-id :created (current-time) :data data})
       nil)))
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
  "Create an in-memory queue with a specified read-timeout for messages.
  Defaults to 300 seconds."
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

(defn memory-state
  "Create an in-memory state."
  []
  (MemoryState. (atom {})))
