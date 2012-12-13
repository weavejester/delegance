(ns delegance.core
  (:require [delegance.protocols :refer :all]
            [delegance.util :refer (random-uuid)]))

(defn- poll-while-nil [rate func]
  (loop []
    (or (func)
        (do (Thread/sleep rate)
            (recur)))))

(defn delegate-eval [client form]
  (let [{queue :queue, state :state, storage :store} client
        job-id  (random-uuid)
        form-id (store storage form)]
    (put state job-id {:form form-id})
    (push queue job-id)
    (delay (poll-while-nil 1000
            #(if-let [result-id (:result (get! state job-id))]
               (fetch storage result-id))))))

(defmacro delegate [client form]
  `(delegate-eval ~client '~form))
