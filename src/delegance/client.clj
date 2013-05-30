(ns delegance.client
  "The Delegance client that sends jobs to the worker processes."
  (:require [delegance.protocols :refer :all]))

(deftype RemotePromise [cache poll no-data poll-rate]
  clojure.lang.IDeref
  (deref [rp]
    (deref rp (* 1000 60 60 24 365 1000) nil))
  clojure.lang.IPending
  (isRealized [_]
    (not= @cache no-data))
  clojure.lang.IBlockingDeref
  (deref [_ timeout timeout-val]
    (let [end-time (+ (System/currentTimeMillis) timeout)]
      (loop []
        (let [cached-value @cache]
          (if (not= cached-value no-data)
            cached-value
            (let [value (poll)]
              (if (not= value no-data)
                (reset! cache value)
                (let [time-left (- end-time (System/currentTimeMillis))]
                  (if (<= time-left 0)
                    timeout-val
                    (do (Thread/sleep (min time-left poll-rate))
                        (recur))))))))))))

(defn- remote-promise
  "Create a RemotePromise ref that polls a function until the result is equal
  to somthing other than the value supplied for no-data."
  ([poll no-data]
     (remote-promise poll no-data 1000))
  ([poll no-data poll-rate]
     (RemotePromise. (atom no-data) poll no-data poll-rate)))

(defn delegate
  "Delegate a quoted Clojure form to be evaluated by a remote worker process.
  Workers can be started using the delegance.worker/run-worker function.
  A promise is returned that will be delivered the result of the evaluation.
  The config argument should be a map specifing a :queue and a :store, which
  should respectively implement the Queue and Store protocols in the
  delegance.protocols namespace."
  [config form]
  (let [{queue :queue, store :store} config
        job-id (java.util.UUID/randomUUID)
        result (promise)]
    (put store job-id {:form form})
    (push queue job-id)
    (remote-promise
     #(if-let [job (get! store job-id)]
        (if (:complete? job)
          (:result job)
          ::no-data))
     ::no-data)))
