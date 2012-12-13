(ns delegance.memory
  (:require [delegance.protocols :refer :all]
            [delegance.util :refer :all]))

(defn- current-time []
  (long (/ (System/currentTimeMillis) 1000)))

(defn- expired? [x]
  (if-let [expires (:expires x)]
    (>= (current-time) expires)))

(deftype MemoryStore [a]
  Store
  (store* [_ val timeout]
    (let [key     (random-uuid)
          expires (+ (current-time) timeout)]
      (swap! a assoc key {:data val :expires expires})
      key))
  (fetch [_ key]
    (if-let [val (@a key)]
      (if (expired? val)
        (do (swap! a dissoc key) nil)
        (:data val)))))

(defn memory-store []
  (MemoryStore. (atom {})))

(deftype MemoryQueue [queue reserved]
  Queue
  (push [_ data]
    (dosync
     (alter queue conj {:id (random-uuid) :created (current-time) :data data})
     nil))
  (reserve [_ timeout]
    (dosync
     (doseq [[id x] @reserved :when (expired? x)]
       (alter reserved dissoc id)
       (alter queue conj (dissoc x :expires)))
     (if-let [val (peek @queue)]
       (let [val (assoc val :expires (+ (current-time) timeout))]
         (alter queue pop)
         (alter reserved assoc (:id val) val)
         val))))
  (finish [_ job]
    (dosync
     (alter reserved dissoc (:id job))
     nil)))

(defn memory-queue []
  (MemoryQueue.
   (ref (clojure.lang.PersistentQueue/EMPTY))
   (ref {})))

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
