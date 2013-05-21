(ns delegance.core-test
  (:use clojure.test
        delegance.core
        delegance.memory
        delegance.worker))

(def client
  {:queue (memory-queue)
   :state (memory-state)})

(deftest test-delegate
  (run-worker client)
  (testing "simple deref"
    (let [x (delegate client (+ 1 1))]
      (is (= @x 2))))
  (testing "nil result"
    (let [x (delegate client nil)]
      (is (nil? (deref x 5000 :fail)))))
  (testing "timeout values"
    (let [x (delegate client (Thread/sleep 3000))]
      (is (= (deref x 1000 :timeout) :timeout))))
  (testing "timeout times"
    (let [x  (delegate client (Thread/sleep 3000))
          t1 (System/currentTimeMillis)]
      (deref x 1500 :timeout)
      (let [t2 (System/currentTimeMillis)]
        (is (<= 1400 (- t2 t1) 1600))))))
