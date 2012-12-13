(ns delegance.core
  (:require [delegance.protocols :refer :all]
            [delegance.util :refer (random-uuid)]))

(defn- poll-while-nil [rate func]
  (loop []
    (or (func)
        (do (Thread/sleep rate)
            (recur)))))

(defn delegate-eval [client form]
  (let [job-id (random-uuid)
        {:keys [queue state store]} client]
    (put state job-id {:form form})
    (push queue job-id)
    (delay (poll-while-nil 1000 #(:result (get! state job-id))))))

(defmacro delegate [client form]
  `(delegate-eval ~client '~form))
