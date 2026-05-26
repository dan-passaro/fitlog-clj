(ns fitlog.main-test
    (:require
     [cljs.test :refer-macros [deftest is testing]]
     [fitlog.main :refer [multiply]]
     ))

(deftest multiply-test
  (is (= 2 (multiply 1 2))))

(deftest multiply-test-2
  (is (= 750 (multiply 10 75))))
