;; SPDX-FileCopyrightText: 2026 Dan Passaro
;; SPDX-License-Identifier: AGPL-3.0-or-later
(ns fitlog.plate-loadout)

(def ^:private plate-sizes [45 35 25 10 5 2.5])

(defn plate-loadout
  "Get the plates needed to load `weight` on an Olympic barbell.

  Assumes a barbell weighs 45lbs.

  weight: int of weight in lbs
  returns: vector of [plate-weight plate-count] pairs
  "
  [weight]
  (->> plate-sizes
       (reduce
        (fn [{:keys [remaining pairs]} plate]
          (let [c (int (quot remaining plate))]
            {:remaining (- remaining (* c plate))
             :pairs (if (> c 0)
                      (conj pairs [plate c])
                      pairs)}))

        ;; subtract weight of the bar, divide by two to get per-side target
        {:remaining (/ (- weight 45) 2) :pairs []})))
