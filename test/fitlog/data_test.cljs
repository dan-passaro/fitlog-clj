;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitlog.data-test
  (:require
   [cljs.test :refer [deftest is testing]]
   [fitlog.data :as d]))

(deftest loads-old-fitlog-data-with-parameters
  ;; Older versions of fitlog used "parameters" instead of "variables"

  (let [sample-data {:exercises
                     [{:name "Treadmill",
                       :parameters [{:name "Speed", :units "mph"} {:name "Time", :units "mins"}]}],
                     :workouts
                     [{:createdAt "2025-11-17T23:02:27.331Z",
                       :id "11047f83-5075-4fd4-9d89-db39f24319aa",
                       :sets
                       [{:exercise
                         {:name "Treadmill",
                          :parameters
                          [{:name "Speed", :units "mph"} {:name "Time", :units "mins"}]},
                         :parameters
                         [[{:name "Speed", :units "mph"} "6.2"]
                          [{:name "Time", :units "mins"} "30"]],
                         :completedAt "2025-11-17T23:02:29.873Z"}]}]}]
    (is (= {:exercises
            [{:name "Treadmill",
              :variables [{:name "Speed", :units "mph"} {:name "Time", :units "mins"}]}],
            :workouts
            [{:createdAt "2025-11-17T23:02:27.331Z",
              :id "11047f83-5075-4fd4-9d89-db39f24319aa",
              :sets
              [{:exercise
                {:name "Treadmill",
                 :variables
                 [{:name "Speed", :units "mph"} {:name "Time", :units "mins"}]},
                :variables
                [[{:name "Speed", :units "mph"} "6.2"]
                 [{:name "Time", :units "mins"} "30"]],
                :completedAt "2025-11-17T23:02:29.873Z"}]}]}
           (d/migrate sample-data)))))
