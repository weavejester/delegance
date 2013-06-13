(ns delegance.worker-test
  (:use clojure.test
        delegance.client
        delegance.memory
        delegance.worker))

(def config
  {:queue (memory-queue)
   :store (memory-store)})

(deftest test-worker
  (let [w (run-worker (assoc config :timeout 100))]
    (try
      (let [x (delegate config `(do (Thread/sleep 200) (+ 1 1)))
            y (delegate config `(do (Thread/sleep 50)  (+ 1 1)))]
        (is (nil? @x))
        (is (= @y 2)))
      (finally (shutdown-worker w)))))